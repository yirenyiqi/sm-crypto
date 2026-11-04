package com.antherd.smcrypto;

import com.antherd.smcrypto.core.Hex;
import com.antherd.smcrypto.core.SM3Digest;
import com.antherd.smcrypto.core.SM4Engine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Java与JS版本联调测试
 * 验证算法实现的互操作性
 */
public class JavaJsInteropTest {

    @Test
    public void testSM3Compatibility() {
        SM3Digest digest = new SM3Digest();

        // 测试向量来自JS版本
        assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0",
                    digest.digestHex("abc"));

        assertEquals("debe9ff92275b8a138604889c18e5a4d6fdb70e5387e5765293dcba39c0c5732",
                    digest.digestHex("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"));

        assertEquals("78e5c78c5322ca174089e58dc7790acf8ce9d542bee6ae4a5a0797d5e356be61",
                    digest.digestHex("你好"));

        System.out.println("✓ SM3与JS版本完全兼容");
    }

    @Test
    public void testSM4EcbCompatibility() {
        String key = "0123456789abcdeffedcba9876543210";
        String plaintext = "hello world! 我是 juneandgreen.";

        // JS版本的标准输出
        String expectedCiphertext = "0e395deb10f6e8a17e17823e1fd9bd98a1bff1df508b5b8a1efb79ec633d1bb129432ac1b74972dbe97bab04f024e89c";

        // Java加密
        String encrypted = SM4Engine.encryptEcb(plaintext, key);
        assertEquals("ECB加密结果应与JS版本一致", expectedCiphertext, encrypted);

        // Java解密
        String decrypted = SM4Engine.decryptEcb(encrypted, key);
        assertEquals("ECB解密应恢复原文", plaintext, decrypted);

        System.out.println("✓ SM4 ECB模式与JS版本完全兼容");
    }

    @Test
    public void testSM4CbcCompatibility() {
        String key = "0123456789abcdeffedcba9876543210";
        String iv = "fedcba98765432100123456789abcdef";
        String plaintext = "hello world! 我是 juneandgreen.";

        // JS版本的标准输出
        String expectedCiphertext = "0d6cfa73c823b2ac0d6a92c564171892000fbea90be7a4d440bc58a9044fcb5f3d1615d91a6dbfb4dfb0c6915071527b";

        // Java加密
        String encrypted = SM4Engine.encryptCbc(plaintext, key, iv);
        assertEquals("CBC加密结果应与JS版本一致", expectedCiphertext, encrypted);

        // Java解密
        String decrypted = SM4Engine.decryptCbc(encrypted, key, iv);
        assertEquals("CBC解密应恢复原文", plaintext, decrypted);

        System.out.println("✓ SM4 CBC模式与JS版本完全兼容");
    }

    @Test
    public void testSM4DifferentIVProducesDifferentCiphertext() {
        String key = "0123456789abcdeffedcba9876543210";
        String iv1 = "fedcba98765432100123456789abcdef";
        String iv2 = "0123456789abcdeffedcba9876543210";
        String plaintext = "hello world! 我是 antherd.";

        // 这是Issue #22的根本问题：不同IV必须产生不同密文
        String encrypted1 = SM4Engine.encryptCbc(plaintext, key, iv1);
        String encrypted2 = SM4Engine.encryptCbc(plaintext, key, iv2);

        System.out.println("IV1密文: " + encrypted1);
        System.out.println("IV2密文: " + encrypted2);

        // 核心验证：密文必须不同
        if (encrypted1.equals(encrypted2)) {
            throw new AssertionError("不同的IV产生了相同的密文！这是Issue #22描述的bug");
        }

        // 验证都能正确解密
        String decrypted1 = SM4Engine.decryptCbc(encrypted1, key, iv1);
        String decrypted2 = SM4Engine.decryptCbc(encrypted2, key, iv2);

        assertEquals(plaintext, decrypted1);
        assertEquals(plaintext, decrypted2);

        System.out.println("✓ Issue #22已修复：不同IV产生不同密文");
    }
}
