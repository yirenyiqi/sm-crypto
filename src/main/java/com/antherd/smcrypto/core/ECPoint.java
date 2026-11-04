package com.antherd.smcrypto.core;

import java.math.BigInteger;

/**
 * 椭圆曲线点 - 使用射影坐标系
 * y^2 = x^3 + ax + b (mod p)
 *
 * @author geyiwei
 */
public class ECPoint {
    private final ECCurve curve;
    private final ECFieldElement x;
    private final ECFieldElement y;
    private final BigInteger z;
    private BigInteger zinv; // z的逆元，延迟计算

    /**
     * 构造椭圆曲线点
     */
    public ECPoint(ECCurve curve, ECFieldElement x, ECFieldElement y, BigInteger z) {
        this.curve = curve;
        this.x = x;
        this.y = y;
        this.z = z == null ? BigInteger.ONE : z;
        this.zinv = null;
    }

    /**
     * 构造仿射坐标点
     */
    public ECPoint(ECCurve curve, ECFieldElement x, ECFieldElement y) {
        this(curve, x, y, BigInteger.ONE);
    }

    /**
     * 获取x坐标（仿射坐标）
     */
    public ECFieldElement getX() {
        if (zinv == null) {
            zinv = z.modInverse(curve.getQ());
        }

        BigInteger xAffine = x.toBigInteger().multiply(zinv).mod(curve.getQ());
        return curve.fromBigInteger(xAffine);
    }

    /**
     * 获取y坐标（仿射坐标）
     */
    public ECFieldElement getY() {
        if (zinv == null) {
            zinv = z.modInverse(curve.getQ());
        }

        BigInteger yAffine = y.toBigInteger().multiply(zinv).mod(curve.getQ());
        return curve.fromBigInteger(yAffine);
    }

    /**
     * 判断是否为无穷远点
     */
    public boolean isInfinity() {
        if (x == null && y == null) return true;
        return z.equals(BigInteger.ZERO) && !y.toBigInteger().equals(BigInteger.ZERO);
    }

    /**
     * 取反（x轴对称点）
     */
    public ECPoint negate() {
        return new ECPoint(curve, x, y.negate(), z);
    }

    /**
     * 点加法
     */
    public ECPoint add(ECPoint b) {
        if (this.isInfinity()) return b;
        if (b.isInfinity()) return this;

        BigInteger x1 = this.x.toBigInteger();
        BigInteger y1 = this.y.toBigInteger();
        BigInteger z1 = this.z;
        BigInteger x2 = b.x.toBigInteger();
        BigInteger y2 = b.y.toBigInteger();
        BigInteger z2 = b.z;
        BigInteger q = curve.getQ();

        // 标准射影坐标系加法公式
        BigInteger w1 = x1.multiply(z2).mod(q);
        BigInteger w2 = x2.multiply(z1).mod(q);
        BigInteger w3 = w1.subtract(w2).mod(q);
        BigInteger w4 = y1.multiply(z2).mod(q);
        BigInteger w5 = y2.multiply(z1).mod(q);
        BigInteger w6 = w4.subtract(w5).mod(q);

        if (w3.equals(BigInteger.ZERO)) {
            if (w6.equals(BigInteger.ZERO)) {
                return this.twice(); // 点相同，计算2P
            }
            return curve.getInfinity(); // 点互为相反数
        }

        BigInteger w7 = w1.add(w2).mod(q);
        BigInteger w8 = z1.multiply(z2).mod(q);
        BigInteger w9 = w3.multiply(w3).mod(q);
        BigInteger w10 = w3.multiply(w9).mod(q);
        BigInteger w11 = w8.multiply(w6.multiply(w6)).subtract(w7.multiply(w9)).mod(q);

        BigInteger x3 = w3.multiply(w11).mod(q);
        BigInteger y3 = w6.multiply(w9.multiply(w1).subtract(w11)).subtract(w4.multiply(w10)).mod(q);
        BigInteger z3 = w10.multiply(w8).mod(q);

        return new ECPoint(curve, curve.fromBigInteger(x3), curve.fromBigInteger(y3), z3);
    }

    /**
     * 点倍乘（2P）
     */
    public ECPoint twice() {
        if (this.isInfinity()) return this;
        if (y.toBigInteger().signum() == 0) return curve.getInfinity();

        BigInteger x1 = this.x.toBigInteger();
        BigInteger y1 = this.y.toBigInteger();
        BigInteger z1 = this.z;
        BigInteger q = curve.getQ();
        BigInteger a = curve.getA().toBigInteger();

        // 标准射影坐标系倍乘公式
        BigInteger w1 = x1.multiply(x1).multiply(BigInteger.valueOf(3))
                        .add(a.multiply(z1.multiply(z1))).mod(q);
        BigInteger w2 = y1.shiftLeft(1).multiply(z1).mod(q);
        BigInteger w3 = y1.multiply(y1).mod(q);
        BigInteger w4 = w3.multiply(x1).multiply(z1).mod(q);
        BigInteger w5 = w2.multiply(w2).mod(q);
        BigInteger w6 = w1.multiply(w1).subtract(w4.shiftLeft(3)).mod(q);

        BigInteger x3 = w2.multiply(w6).mod(q);
        BigInteger y3 = w1.multiply(w4.shiftLeft(2).subtract(w6))
                        .subtract(w5.shiftLeft(1).multiply(w3)).mod(q);
        BigInteger z3 = w2.multiply(w5).mod(q);

        return new ECPoint(curve, curve.fromBigInteger(x3), curve.fromBigInteger(y3), z3);
    }

    /**
     * 点乘法（kP）- 使用加减法
     */
    public ECPoint multiply(BigInteger k) {
        if (this.isInfinity()) return this;
        if (k.signum() == 0) return curve.getInfinity();

        // 使用3k技巧提升性能
        BigInteger k3 = k.multiply(BigInteger.valueOf(3));
        ECPoint neg = this.negate();
        ECPoint Q = this;

        for (int i = k3.bitLength() - 2; i > 0; i--) {
            Q = Q.twice();

            boolean k3Bit = k3.testBit(i);
            boolean kBit = k.testBit(i);

            if (k3Bit != kBit) {
                Q = Q.add(k3Bit ? this : neg);
            }
        }

        return Q;
    }

    /**
     * 判断两点是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ECPoint)) return false;

        ECPoint other = (ECPoint) obj;
        if (this.isInfinity()) return other.isInfinity();
        if (other.isInfinity()) return false;

        BigInteger q = curve.getQ();

        // u = y2 * z1 - y1 * z2
        BigInteger u = other.y.toBigInteger().multiply(this.z)
                      .subtract(this.y.toBigInteger().multiply(other.z)).mod(q);
        if (!u.equals(BigInteger.ZERO)) return false;

        // v = x2 * z1 - x1 * z2
        BigInteger v = other.x.toBigInteger().multiply(this.z)
                      .subtract(this.x.toBigInteger().multiply(other.z)).mod(q);
        return v.equals(BigInteger.ZERO);
    }

    @Override
    public int hashCode() {
        if (isInfinity()) return 0;
        return getX().hashCode() ^ getY().hashCode();
    }

    /**
     * 获取所属曲线
     */
    public ECCurve getCurve() {
        return curve;
    }
}
