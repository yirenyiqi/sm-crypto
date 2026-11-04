package com.antherd.smcrypto;

import com.antherd.smcrypto.core.Hex;
import com.antherd.smcrypto.core.SM3Digest;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * SM3算法纯Java实现测试
 */
public class SM3DigestTest {

    @Test
    public void testSM3Basic() {
        SM3Digest digest = new SM3Digest();
        String result = digest.digestHex("abc");

        // 标准测试向量：SM3("abc")
        String expected = "66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0";
        assertEquals("SM3('abc') should match standard test vector", expected, result);

        System.out.println("SM3(abc): " + result);
    }

    @Test
    public void testSM3Empty() {
        SM3Digest digest = new SM3Digest();
        String result = digest.digestHex("");

        // SM3("")
        String expected = "1ab21d8355cfa17f8e61194831e81a8f22bec8c728fefb747ed035eb5082aa2b";
        assertEquals("SM3('') should match", expected, result);
    }

    @Test
    public void testSM3LongString() {
        SM3Digest digest = new SM3Digest();
        String input = "abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd";
        String result = digest.digestHex(input);

        System.out.println("SM3(long string): " + result);

        // 验证长度
        assertEquals("Hash length should be 64 hex chars", 64, result.length());
    }

    @Test
    public void testSM3Chinese() {
        SM3Digest digest = new SM3Digest();
        String input = "中文测试";
        String result = digest.digestHex(input);

        System.out.println("SM3(中文测试): " + result);
        assertEquals("Hash length should be 64 hex chars", 64, result.length());
    }

    @Test
    public void testHMAC() {
        byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
        byte[] key = Hex.decode("0123456789abcdef");

        byte[] hmac = SM3Digest.hmac(input, key);
        String result = Hex.encode(hmac);

        System.out.println("HMAC-SM3: " + result);
        assertEquals("HMAC length should be 64 hex chars", 64, result.length());
    }
}
