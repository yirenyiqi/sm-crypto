package com.antherd.smcrypto.core;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * SM2签名验签
 *
 * @author geyiwei
 */
public class SM2Signature {
    private static final String DEFAULT_USER_ID = "1234567812345678";

    /**
     * SM2签名
     *
     * @param message 消息
     * @param privateKeyHex 私钥
     * @return 签名（r||s，128位16进制）
     */
    public static String sign(String message, String privateKeyHex) {
        return sign(message, privateKeyHex, null, DEFAULT_USER_ID, false);
    }

    /**
     * SM2签名（完整参数）
     *
     * @param message 消息
     * @param privateKeyHex 私钥
     * @param publicKeyHex 公钥（可选，为null则从私钥计算）
     * @param userId 用户ID
     * @param hashMessage 是否对消息进行SM3杂凑
     * @return 签名
     */
    public static String sign(String message, String privateKeyHex, String publicKeyHex,
                             String userId, boolean hashMessage) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        return signBytes(msgBytes, privateKeyHex, publicKeyHex, userId, hashMessage);
    }

    /**
     * SM2签名（字节数组）
     */
    public static String signBytes(byte[] message, String privateKeyHex, String publicKeyHex,
                                   String userId, boolean hashMessage) {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        BigInteger n = SM2KeyPair.SM2Params.N;

        // 计算公钥
        if (publicKeyHex == null) {
            publicKeyHex = SM2KeyPair.getPublicKeyFromPrivate(privateKeyHex);
        }

        // 计算消息摘要e
        byte[] eBytes;
        if (hashMessage) {
            eBytes = getZ(publicKeyHex, userId);
            SM3Digest digest = new SM3Digest();
            byte[] combined = new byte[eBytes.length + message.length];
            System.arraycopy(eBytes, 0, combined, 0, eBytes.length);
            System.arraycopy(message, 0, combined, eBytes.length, message.length);
            eBytes = digest.digest(combined);
        } else {
            eBytes = message;
        }

        BigInteger e = new BigInteger(1, eBytes);

        // 生成随机数k并计算签名
        BigInteger r, s;
        SecureRandom random = new SecureRandom();

        do {
            BigInteger k;
            ECPoint kG;

            do {
                // 生成随机数k ∈ [1, n-1]
                do {
                    k = new BigInteger(n.bitLength(), random);
                } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(n) >= 0);

                // 计算(x1, y1) = k * G
                kG = SM2KeyPair.SM2Params.getG().multiply(k);
                BigInteger x1 = kG.getX().toBigInteger();

                // r = (e + x1) mod n
                r = e.add(x1).mod(n);

            } while (r.equals(BigInteger.ZERO) || r.add(k).equals(n));

            // s = (d + 1)^-1 * (k - r * d) mod n
            BigInteger dPlus1Inv = d.add(BigInteger.ONE).modInverse(n);
            s = dPlus1Inv.multiply(k.subtract(r.multiply(d))).mod(n);

        } while (s.equals(BigInteger.ZERO));

        return Hex.leftPad(r.toString(16), 64) + Hex.leftPad(s.toString(16), 64);
    }

    /**
     * SM2验签
     */
    public static boolean verify(String message, String signatureHex, String publicKeyHex) {
        return verify(message, signatureHex, publicKeyHex, DEFAULT_USER_ID, false);
    }

    /**
     * SM2验签（完整参数）
     */
    public static boolean verify(String message, String signatureHex, String publicKeyHex,
                                String userId, boolean hashMessage) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        return verifyBytes(msgBytes, signatureHex, publicKeyHex, userId, hashMessage);
    }

    /**
     * SM2验签（字节数组）
     */
    public static boolean verifyBytes(byte[] message, String signatureHex, String publicKeyHex,
                                     String userId, boolean hashMessage) {
        // 验证公钥
        if (!SM2KeyPair.verifyPublicKey(publicKeyHex)) {
            return false;
        }

        // 解析签名
        if (signatureHex.length() != 128) {
            return false;
        }

        BigInteger r = new BigInteger(signatureHex.substring(0, 64), 16);
        BigInteger s = new BigInteger(signatureHex.substring(64), 16);

        BigInteger n = SM2KeyPair.SM2Params.N;
        BigInteger nSubOne = n.subtract(BigInteger.ONE);

        // 验证r, s ∈ [1, n-1]
        if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(nSubOne) > 0) {
            return false;
        }
        if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(nSubOne) > 0) {
            return false;
        }

        // 计算消息摘要e
        byte[] eBytes;
        if (hashMessage) {
            eBytes = getZ(publicKeyHex, userId);
            SM3Digest digest = new SM3Digest();
            byte[] combined = new byte[eBytes.length + message.length];
            System.arraycopy(eBytes, 0, combined, 0, eBytes.length);
            System.arraycopy(message, 0, combined, eBytes.length, message.length);
            eBytes = digest.digest(combined);
        } else {
            eBytes = message;
        }

        BigInteger e = new BigInteger(1, eBytes);

        // t = (r + s) mod n
        BigInteger t = r.add(s).mod(n);
        if (t.equals(BigInteger.ZERO)) {
            return false;
        }

        // (x1, y1) = s*G + t*PA
        ECCurve curve = SM2KeyPair.SM2Params.getCurve();
        ECPoint PA = curve.decodePoint(publicKeyHex);

        ECPoint sG = SM2KeyPair.SM2Params.getG().multiply(s);
        ECPoint tPA = PA.multiply(t);
        ECPoint result = sG.add(tPA);

        BigInteger x1 = result.getX().toBigInteger();

        // R = (e + x1) mod n
        BigInteger R = e.add(x1).mod(n);

        return r.equals(R);
    }

    /**
     * 计算Z = SM3(ENTL || ID || a || b || xG || yG || xA || yA)
     */
    private static byte[] getZ(String publicKeyHex, String userId) {
        if (userId == null || userId.isEmpty()) {
            userId = DEFAULT_USER_ID;
        }

        byte[] idBytes = userId.getBytes(StandardCharsets.UTF_8);
        int entl = idBytes.length * 8;

        ECCurve curve = SM2KeyPair.SM2Params.getCurve();
        BigInteger a = curve.getA().toBigInteger();
        BigInteger b = curve.getB().toBigInteger();
        BigInteger gx = SM2KeyPair.SM2Params.GX;
        BigInteger gy = SM2KeyPair.SM2Params.GY;

        // 解析公钥
        String px, py;
        if (publicKeyHex.length() == 130) {
            px = publicKeyHex.substring(2, 66);
            py = publicKeyHex.substring(66);
        } else {
            ECPoint point = curve.decodePoint(publicKeyHex);
            px = Hex.leftPad(point.getX().toBigInteger().toString(16), 64);
            py = Hex.leftPad(point.getY().toBigInteger().toString(16), 64);
        }

        // 拼接数据
        String hexData = String.format("%04x", entl)
                       + Hex.encode(idBytes)
                       + Hex.leftPad(a.toString(16), 64)
                       + Hex.leftPad(b.toString(16), 64)
                       + Hex.leftPad(gx.toString(16), 64)
                       + Hex.leftPad(gy.toString(16), 64)
                       + px + py;

        SM3Digest digest = new SM3Digest();
        return digest.digest(Hex.decode(hexData));
    }
}
