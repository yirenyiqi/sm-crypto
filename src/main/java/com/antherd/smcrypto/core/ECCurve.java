package com.antherd.smcrypto.core;

import java.math.BigInteger;

/**
 * 椭圆曲线 y^2 = x^3 + ax + b (mod p)
 * SM2使用的是素数域上的椭圆曲线
 *
 * @author geyiwei
 */
public class ECCurve {
    private final BigInteger q; // 素数模数
    private final ECFieldElement a; // 曲线参数a
    private final ECFieldElement b; // 曲线参数b
    private final ECPoint infinity; // 无穷远点

    public ECCurve(BigInteger q, BigInteger a, BigInteger b) {
        this.q = q;
        this.a = fromBigInteger(a);
        this.b = fromBigInteger(b);
        this.infinity = new ECPoint(this, null, null, BigInteger.ZERO);
    }

    /**
     * 从BigInteger创建域元素
     */
    public ECFieldElement fromBigInteger(BigInteger x) {
        return new ECFieldElement(q, x);
    }

    /**
     * 解析16进制串为椭圆曲线点
     */
    public ECPoint decodePoint(String hex) {
        if (hex == null || hex.length() < 2 || hex.length() % 2 != 0) {
            return null;
        }

        int prefix = Integer.parseInt(hex.substring(0, 2), 16);

        switch (prefix) {
            case 0x00:
                // 无穷远点
                return hex.length() == 2 ? infinity : null;

            case 0x02:
            case 0x03:
                // 压缩公钥
                if (hex.length() != 66) return null;
                return decompressPoint(hex);

            case 0x04:
            case 0x06:
            case 0x07:
                // 未压缩公钥
                if (hex.length() != 130) return null;

                String xHex = hex.substring(2, 66);
                String yHex = hex.substring(66, 130);

                ECFieldElement x = fromBigInteger(new BigInteger(xHex, 16));
                ECFieldElement y = fromBigInteger(new BigInteger(yHex, 16));

                return new ECPoint(this, x, y);

            default:
                return null;
        }
    }

    /**
     * 解压缩公钥
     */
    private ECPoint decompressPoint(String hex) {
        int yTilde = Integer.parseInt(hex.substring(0, 2), 16) - 2;
        BigInteger xValue = new BigInteger(hex.substring(2), 16);

        ECFieldElement x = fromBigInteger(xValue);

        // 计算 y^2 = x^3 + ax + b
        ECFieldElement ySquare = x.multiply(x.square()).add(x.multiply(a)).add(b);

        // 对于p ≡ 3 (mod 4)，y = (y^2)^((p+1)/4) mod p
        BigInteger yValue = ySquare.toBigInteger().modPow(
            q.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), q
        );

        // 检查y的奇偶性是否匹配
        if (yValue.mod(BigInteger.valueOf(2)).intValue() != yTilde) {
            yValue = q.subtract(yValue);
        }

        ECFieldElement y = fromBigInteger(yValue);
        return new ECPoint(this, x, y);
    }

    /**
     * 验证点是否在曲线上
     */
    public boolean isOnCurve(ECPoint point) {
        if (point.isInfinity()) return true;

        ECFieldElement x = point.getX();
        ECFieldElement y = point.getY();

        // 验证 y^2 = x^3 + ax + b
        ECFieldElement lhs = y.square();
        ECFieldElement rhs = x.multiply(x.square()).add(x.multiply(a)).add(b);

        return lhs.equals(rhs);
    }

    /**
     * 压缩公钥
     */
    public String compressPoint(ECPoint point) {
        if (point.isInfinity()) {
            return "00";
        }

        BigInteger x = point.getX().toBigInteger();
        BigInteger y = point.getY().toBigInteger();

        // 根据y的奇偶性选择前缀
        int prefix = y.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) ? 0x02 : 0x03;

        return String.format("%02x%064x", prefix, x);
    }

    public BigInteger getQ() {
        return q;
    }

    public ECFieldElement getA() {
        return a;
    }

    public ECFieldElement getB() {
        return b;
    }

    public ECPoint getInfinity() {
        return infinity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ECCurve)) return false;

        ECCurve other = (ECCurve) obj;
        return q.equals(other.q) && a.equals(other.a) && b.equals(other.b);
    }
}
