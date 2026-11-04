package com.antherd.smcrypto;

import com.antherd.smcrypto.core.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SM2加密解密测试
 */
public class SM2EngineTest {

    @Test
    public void testEncryptDecrypt() {
        // 生成密钥对
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String plaintext = "hello world! 测试SM2加密";

        // 加密
        String ciphertext = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);
        assertNotNull("密文不应为空", ciphertext);
        assertTrue("密文长度应大于明文", ciphertext.length() > plaintext.length() * 2);

        System.out.println("明文: " + plaintext);
        System.out.println("密文: " + ciphertext);

        // 解密
        String decrypted = SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C3C2);
        assertEquals("解密应恢复原文", plaintext, decrypted);

        System.out.println("解密: " + decrypted);
    }

    @Test
    public void testEncryptDecryptC1C2C3() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String plaintext = "测试C1C2C3模式";

        String ciphertext = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C2C3);
        String decrypted = SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C2C3);

        assertEquals("C1C2C3模式解密", plaintext, decrypted);
        System.out.println("✓ C1C2C3模式测试通过");
    }

    @Test
    public void testEncryptWithCompressedPublicKey() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String compressedKey = SM2KeyPair.compressPublicKey(kp.getPublicKey());

        String plaintext = "测试压缩公钥加密";

        // 使用压缩公钥加密
        String ciphertext = SM2Engine.encrypt(plaintext, compressedKey, SM2Engine.C1C3C2);

        // 解密
        String decrypted = SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C3C2);

        assertEquals("压缩公钥加密解密", plaintext, decrypted);
        System.out.println("✓ 压缩公钥加密测试通过");
    }

    @Test
    public void testEncryptLongText() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("这是第").append(i).append("行测试数据。");
        }
        String plaintext = sb.toString();

        String ciphertext = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);
        String decrypted = SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C3C2);

        assertEquals("长文本加密解密", plaintext, decrypted);
        System.out.println("✓ 长文本加密测试通过，长度: " + plaintext.length());
    }

    @Test
    public void testMultipleEncryptions() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String plaintext = "测试多次加密";

        // 多次加密应产生不同密文（因为每次随机数k不同）
        String cipher1 = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);
        String cipher2 = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);

        assertNotEquals("不同的随机数应产生不同密文", cipher1, cipher2);

        // 但都能正确解密
        assertEquals(plaintext, SM2Engine.decrypt(cipher1, kp.getPrivateKey(), SM2Engine.C1C3C2));
        assertEquals(plaintext, SM2Engine.decrypt(cipher2, kp.getPrivateKey(), SM2Engine.C1C3C2));

        System.out.println("✓ 多次加密测试通过");
    }
}
