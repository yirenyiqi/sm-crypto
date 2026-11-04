package com.antherd.smcrypto.core;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * SM2加密解密引擎
 *
 * @author geyiwei
 */
public class SM2Engine {
    public static final int C1C2C3 = 0;
    public static final int C1C3C2 = 1;

    /**
     * SM2加密
     *
     * @param plaintext 明文
     * @param publicKeyHex 公钥（16进制）
     * @param cipherMode 密文模式：0-C1C2C3, 1-C1C3C2
     * @return 密文（16进制）
     */
    public static String encrypt(String plaintext, String publicKeyHex, int cipherMode) {
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        return encrypt(data, publicKeyHex, cipherMode);
    }

    /**
     * SM2加密（字节数组）
     */
    public static String encrypt(byte[] data, String publicKeyHex, int cipherMode) {
        // 验证公钥
        if (!SM2KeyPair.verifyPublicKey(publicKeyHex)) {
            throw new IllegalArgumentException("Invalid public key");
        }

        ECCurve curve = SM2KeyPair.SM2Params.getCurve();
        ECPoint publicKey = curve.decodePoint(publicKeyHex);

        // 生成随机数k
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        BigInteger k = new BigInteger(kp.getPrivateKey(), 16);

        // C1 = k * G（取x,y坐标）
        String c1Hex = kp.getPublicKey();
        if (c1Hex.length() > 128) {
            c1Hex = c1Hex.substring(c1Hex.length() - 128);
        }

        // (x2, y2) = k * publicKey
        ECPoint kP = publicKey.multiply(k);
        BigInteger x2 = kP.getX().toBigInteger();
        BigInteger y2 = kP.getY().toBigInteger();

        byte[] x2Bytes = toByteArray32(x2);
        byte[] y2Bytes = toByteArray32(y2);

        // C3 = SM3(x2 || M || y2)
        SM3Digest digest = new SM3Digest();
        byte[] c3Data = new byte[x2Bytes.length + data.length + y2Bytes.length];
        System.arraycopy(x2Bytes, 0, c3Data, 0, x2Bytes.length);
        System.arraycopy(data, 0, c3Data, x2Bytes.length, data.length);
        System.arraycopy(y2Bytes, 0, c3Data, x2Bytes.length + data.length, y2Bytes.length);
        byte[] c3 = digest.digest(c3Data);
        String c3Hex = Hex.encode(c3);

        // 生成密钥流t = KDF(x2 || y2, klen)
        byte[] c2 = kdf(x2Bytes, y2Bytes, data.length);

        // C2 = M ⊕ t
        for (int i = 0; i < data.length; i++) {
            c2[i] ^= data[i];
        }
        String c2Hex = Hex.encode(c2);

        // 返回C1 || C3 || C2 或 C1 || C2 || C3
        return cipherMode == C1C2C3 ? c1Hex + c2Hex + c3Hex : c1Hex + c3Hex + c2Hex;
    }

    /**
     * SM2解密
     */
    public static String decrypt(String cipherHex, String privateKeyHex, int cipherMode) {
        byte[] result = decryptToBytes(cipherHex, privateKeyHex, cipherMode);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * SM2解密（返回字节数组）
     */
    public static byte[] decryptToBytes(String cipherHex, String privateKeyHex, int cipherMode) {
        BigInteger d = new BigInteger(privateKeyHex, 16);

        // 解析C1, C2, C3
        String c1Hex = cipherHex.substring(0, 128);
        String c3Hex, c2Hex;

        if (cipherMode == C1C2C3) {
            c3Hex = cipherHex.substring(cipherHex.length() - 64);
            c2Hex = cipherHex.substring(128, cipherHex.length() - 64);
        } else {
            c3Hex = cipherHex.substring(128, 192);
            c2Hex = cipherHex.substring(192);
        }

        // 解析C1为椭圆曲线点
        ECCurve curve = SM2KeyPair.SM2Params.getCurve();
        ECPoint c1 = curve.decodePoint("04" + c1Hex);

        if (c1 == null || !curve.isOnCurve(c1)) {
            throw new IllegalArgumentException("Invalid cipher text");
        }

        // 计算 (x2, y2) = d * C1
        ECPoint dC1 = c1.multiply(d);
        BigInteger x2 = dC1.getX().toBigInteger();
        BigInteger y2 = dC1.getY().toBigInteger();

        byte[] x2Bytes = toByteArray32(x2);
        byte[] y2Bytes = toByteArray32(y2);

        // 解密C2
        byte[] c2 = Hex.decode(c2Hex);
        byte[] t = kdf(x2Bytes, y2Bytes, c2.length);

        byte[] M = new byte[c2.length];
        for (int i = 0; i < c2.length; i++) {
            M[i] = (byte) (c2[i] ^ t[i]);
        }

        // 验证C3
        SM3Digest digest = new SM3Digest();
        byte[] c3Data = new byte[x2Bytes.length + M.length + y2Bytes.length];
        System.arraycopy(x2Bytes, 0, c3Data, 0, x2Bytes.length);
        System.arraycopy(M, 0, c3Data, x2Bytes.length, M.length);
        System.arraycopy(y2Bytes, 0, c3Data, x2Bytes.length + M.length, y2Bytes.length);
        byte[] checkC3 = digest.digest(c3Data);

        if (!Hex.encode(checkC3).equalsIgnoreCase(c3Hex)) {
            throw new IllegalArgumentException("Decryption failed: MAC check failed");
        }

        return M;
    }

    /**
     * 密钥派生函数KDF
     */
    private static byte[] kdf(byte[] x2, byte[] y2, int keyLen) {
        SM3Digest digest = new SM3Digest();
        byte[] z = new byte[x2.length + y2.length];
        System.arraycopy(x2, 0, z, 0, x2.length);
        System.arraycopy(y2, 0, z, x2.length, y2.length);

        byte[] result = new byte[keyLen];
        int ct = 1;
        int offset = 0;

        while (offset < keyLen) {
            byte[] ctBytes = new byte[z.length + 4];
            System.arraycopy(z, 0, ctBytes, 0, z.length);
            ctBytes[z.length] = (byte) ((ct >>> 24) & 0xff);
            ctBytes[z.length + 1] = (byte) ((ct >>> 16) & 0xff);
            ctBytes[z.length + 2] = (byte) ((ct >>> 8) & 0xff);
            ctBytes[z.length + 3] = (byte) (ct & 0xff);

            byte[] hash = digest.digest(ctBytes);

            int copyLen = Math.min(hash.length, keyLen - offset);
            System.arraycopy(hash, 0, result, offset, copyLen);

            offset += copyLen;
            ct++;
        }

        return result;
    }

    /**
     * BigInteger转32字节数组
     */
    private static byte[] toByteArray32(BigInteger bn) {
        byte[] bytes = bn.toByteArray();

        if (bytes.length == 32) {
            return bytes;
        } else if (bytes.length > 32) {
            // 去除前导0
            byte[] result = new byte[32];
            System.arraycopy(bytes, bytes.length - 32, result, 0, 32);
            return result;
        } else {
            // 补齐到32字节
            byte[] result = new byte[32];
            System.arraycopy(bytes, 0, result, 32 - bytes.length, bytes.length);
            return result;
        }
    }
}
