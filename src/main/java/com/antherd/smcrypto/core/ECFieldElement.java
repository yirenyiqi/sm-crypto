package com.antherd.smcrypto.core;

import java.math.BigInteger;

/**
 * 椭圆曲线域元素 - 有限域Fp上的元素
 *
 * @author geyiwei
 */
public class ECFieldElement {
    private final BigInteger q; // 有限域的模数
    private final BigInteger x; // 元素值

    public ECFieldElement(BigInteger q, BigInteger x) {
        this.q = q;
        this.x = x.mod(q);
    }

    /**
     * 获取元素值
     */
    public BigInteger toBigInteger() {
        return x;
    }

    /**
     * 取反
     */
    public ECFieldElement negate() {
        return new ECFieldElement(q, x.negate().mod(q));
    }

    /**
     * 相加
     */
    public ECFieldElement add(ECFieldElement b) {
        return new ECFieldElement(q, x.add(b.x).mod(q));
    }

    /**
     * 相减
     */
    public ECFieldElement subtract(ECFieldElement b) {
        return new ECFieldElement(q, x.subtract(b.x).mod(q));
    }

    /**
     * 相乘
     */
    public ECFieldElement multiply(ECFieldElement b) {
        return new ECFieldElement(q, x.multiply(b.x).mod(q));
    }

    /**
     * 相除
     */
    public ECFieldElement divide(ECFieldElement b) {
        return new ECFieldElement(q, x.multiply(b.x.modInverse(q)).mod(q));
    }

    /**
     * 平方
     */
    public ECFieldElement square() {
        return new ECFieldElement(q, x.multiply(x).mod(q));
    }

    /**
     * 判断相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ECFieldElement)) return false;

        ECFieldElement other = (ECFieldElement) obj;
        return q.equals(other.q) && x.equals(other.x);
    }

    @Override
    public int hashCode() {
        return x.hashCode() ^ q.hashCode();
    }
}
