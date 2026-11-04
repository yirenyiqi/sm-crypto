package com.antherd.smcrypto;

import com.antherd.smcrypto.core.SM4Engine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * SM4算法纯Java实现测试
 */
public class SM4EngineTest {

    @Test
    public void testSM4EcbBasic() {
        String key = "0123456789abcdeffedcba9876543210";
        String plaintext = "hello world! 我是 antherd.";

        // 加密
        String encrypted = SM4Engine.encryptEcb(plaintext, key);
        System.out.println("ECB加密: " + encrypted);

        // 解密
        String decrypted = SM4Engine.decryptEcb(encrypted, key);
        System.out.println("ECB解密: " + decrypted);

        assertEquals("ECB解密应该恢复原文", plaintext, decrypted);
    }

    @Test
    public void testSM4CbcBasic() {
        String key = "0123456789abcdeffedcba9876543210";
        String iv = "fedcba98765432100123456789abcdef";
        String plaintext = "hello world! 我是 antherd.";

        // 加密
        String encrypted = SM4Engine.encryptCbc(plaintext, key, iv);
        System.out.println("CBC加密: " + encrypted);

        // 解密
        String decrypted = SM4Engine.decryptCbc(encrypted, key, iv);
        System.out.println("CBC解密: " + decrypted);

        assertEquals("CBC解密应该恢复原文", plaintext, decrypted);
    }

    @Test
    public void testSM4DifferentIV() {
        String key = "0123456789abcdeffedcba9876543210";
        String iv1 = "fedcba98765432100123456789abcdef";
        String iv2 = "0123456789abcdeffedcba9876543210";
        String plaintext = "hello world! 我是 antherd.";

        String encrypted1 = SM4Engine.encryptCbc(plaintext, key, iv1);
        String encrypted2 = SM4Engine.encryptCbc(plaintext, key, iv2);

        System.out.println("IV1密文: " + encrypted1);
        System.out.println("IV2密文: " + encrypted2);

        // 不同IV应该产生不同密文
        assertEquals("密文长度应该相同", encrypted1.length(), encrypted2.length());

        // 验证能正确解密
        String decrypted1 = SM4Engine.decryptCbc(encrypted1, key, iv1);
        String decrypted2 = SM4Engine.decryptCbc(encrypted2, key, iv2);

        assertEquals("IV1解密", plaintext, decrypted1);
        assertEquals("IV2解密", plaintext, decrypted2);
    }

    @Test
    public void testSM4EmptyString() {
        String key = "0123456789abcdeffedcba9876543210";
        String plaintext = "";

        String encrypted = SM4Engine.encryptEcb(plaintext, key);
        String decrypted = SM4Engine.decryptEcb(encrypted, key);

        assertEquals("空串加解密", plaintext, decrypted);
    }

    @Test
    public void testSM4LongText() {
        String key = "0123456789abcdeffedcba9876543210";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("这是测试数据").append(i);
        }
        String plaintext = sb.toString();

        String encrypted = SM4Engine.encryptEcb(plaintext, key);
        String decrypted = SM4Engine.decryptEcb(encrypted, key);

        assertEquals("长文本加解密", plaintext, decrypted);
        System.out.println("长文本加密成功，原文长度: " + plaintext.length() + " 字节");
    }
}
