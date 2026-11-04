package com.antherd.smcrypto.core;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * SM2椭圆曲线密钥对
 *
 * @author geyiwei
 */
public class SM2KeyPair {
    private final String privateKey; // 64位16进制私钥
    private final String publicKey;  // 130位16进制未压缩公钥

    public SM2KeyPair(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    /**
     * SM2推荐曲线参数
     */
    public static class SM2Params {
        // 素数模数p
        public static final BigInteger P = new BigInteger(
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);

        // 曲线参数a
        public static final BigInteger A = new BigInteger(
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);

        // 曲线参数b
        public static final BigInteger B = new BigInteger(
            "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);

        // 基点G的x坐标
        public static final BigInteger GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);

        // 基点G的y坐标
        public static final BigInteger GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

        // 基点的阶n
        public static final BigInteger N = new BigInteger(
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);

        private static ECCurve curve;
        private static ECPoint G;

        /**
         * 获取SM2曲线
         */
        public static ECCurve getCurve() {
            if (curve == null) {
                curve = new ECCurve(P, A, B);
            }
            return curve;
        }

        /**
         * 获取基点G
         */
        public static ECPoint getG() {
            if (G == null) {
                ECCurve c = getCurve();
                G = new ECPoint(c, c.fromBigInteger(GX), c.fromBigInteger(GY));
            }
            return G;
        }
    }

    /**
     * 生成SM2密钥对
     */
    public static SM2KeyPair generateKeyPair() {
        SecureRandom random = new SecureRandom();
        BigInteger n = SM2Params.N;

        // 生成随机私钥 d ∈ [1, n-1]
        BigInteger d;
        do {
            d = new BigInteger(n.bitLength(), random);
        } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(n) >= 0);

        String privateKey = Hex.leftPad(d.toString(16), 64);

        // 计算公钥 P = dG
        ECPoint P = SM2Params.getG().multiply(d);
        BigInteger px = P.getX().toBigInteger();
        BigInteger py = P.getY().toBigInteger();

        String publicKey = "04" + Hex.leftPad(px.toString(16), 64)
                                + Hex.leftPad(py.toString(16), 64);

        return new SM2KeyPair(privateKey, publicKey);
    }

    /**
     * 从私钥计算公钥
     */
    public static String getPublicKeyFromPrivate(String privateKeyHex) {
        BigInteger d = new BigInteger(privateKeyHex, 16);

        ECPoint P = SM2Params.getG().multiply(d);
        BigInteger px = P.getX().toBigInteger();
        BigInteger py = P.getY().toBigInteger();

        return "04" + Hex.leftPad(px.toString(16), 64)
                    + Hex.leftPad(py.toString(16), 64);
    }

    /**
     * 验证公钥是否有效
     */
    public static boolean verifyPublicKey(String publicKeyHex) {
        if (publicKeyHex == null || publicKeyHex.isEmpty()) {
            return false;
        }

        ECCurve curve = SM2Params.getCurve();
        ECPoint point = curve.decodePoint(publicKeyHex);

        if (point == null || point.isInfinity()) {
            return false;
        }

        return curve.isOnCurve(point);
    }

    /**
     * 压缩公钥
     */
    public static String compressPublicKey(String uncompressedKey) {
        if (uncompressedKey.length() != 130) {
            throw new IllegalArgumentException("Invalid uncompressed public key");
        }

        ECCurve curve = SM2Params.getCurve();
        ECPoint point = curve.decodePoint(uncompressedKey);

        if (point == null) {
            throw new IllegalArgumentException("Invalid public key");
        }

        return curve.compressPoint(point);
    }
}
