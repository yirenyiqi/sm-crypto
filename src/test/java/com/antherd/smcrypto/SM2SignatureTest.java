package com.antherd.smcrypto;

import com.antherd.smcrypto.core.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SM2签名验签测试
 */
public class SM2SignatureTest {

    @Test
    public void testSignAndVerify() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String message = "hello world! 测试SM2签名";

        // 签名
        String signature = SM2Signature.sign(message, kp.getPrivateKey());
        assertNotNull("签名不应为空", signature);
        assertEquals("签名应为128位16进制", 128, signature.length());

        System.out.println("消息: " + message);
        System.out.println("签名: " + signature);

        // 验签
        boolean valid = SM2Signature.verify(message, signature, kp.getPublicKey());
        assertTrue("签名应验证通过", valid);

        System.out.println("✓ 签名验证通过");
    }

    @Test
    public void testVerifyWithWrongSignature() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String message = "测试消息";

        String signature = SM2Signature.sign(message, kp.getPrivateKey());

        // 篡改签名
        String wrongSig = "0" + signature.substring(1);

        boolean valid = SM2Signature.verify(message, wrongSig, kp.getPublicKey());
        assertFalse("篡改的签名应验证失败", valid);

        System.out.println("✓ 错误签名验证失败（符合预期）");
    }

    @Test
    public void testVerifyWithWrongMessage() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String message = "原始消息";

        String signature = SM2Signature.sign(message, kp.getPrivateKey());

        // 篡改消息
        String wrongMsg = "篡改后的消息";

        boolean valid = SM2Signature.verify(wrongMsg, signature, kp.getPublicKey());
        assertFalse("篡改消息后签名应验证失败", valid);

        System.out.println("✓ 消息篡改检测通过");
    }

    @Test
    public void testSignWithCompressedPublicKey() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String compressedKey = SM2KeyPair.compressPublicKey(kp.getPublicKey());

        String message = "测试压缩公钥验签";
        String signature = SM2Signature.sign(message, kp.getPrivateKey(), compressedKey, null, false);

        // 使用压缩公钥验签
        boolean valid = SM2Signature.verify(message, signature, compressedKey);
        assertTrue("压缩公钥应能正确验签", valid);

        // 使用未压缩公钥也应验签通过
        valid = SM2Signature.verify(message, signature, kp.getPublicKey());
        assertTrue("未压缩公钥也应能验签", valid);

        System.out.println("✓ 压缩公钥验签测试通过");
    }

    @Test
    public void testMultipleSignatures() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String message = "测试多次签名";

        // 多次签名应产生不同签名（因为随机数k不同）
        String sig1 = SM2Signature.sign(message, kp.getPrivateKey());
        String sig2 = SM2Signature.sign(message, kp.getPrivateKey());

        assertNotEquals("不同的随机数应产生不同签名", sig1, sig2);

        // 但都应验证通过
        assertTrue(SM2Signature.verify(message, sig1, kp.getPublicKey()));
        assertTrue(SM2Signature.verify(message, sig2, kp.getPublicKey()));

        System.out.println("✓ 多次签名测试通过");
    }

    @Test
    public void testSignWithDifferentKeys() {
        SM2KeyPair kp1 = SM2KeyPair.generateKeyPair();
        SM2KeyPair kp2 = SM2KeyPair.generateKeyPair();
        String message = "测试不同密钥";

        // 用密钥1签名
        String signature = SM2Signature.sign(message, kp1.getPrivateKey());

        // 用密钥1的公钥验证应通过
        assertTrue(SM2Signature.verify(message, signature, kp1.getPublicKey()));

        // 用密钥2的公钥验证应失败
        assertFalse(SM2Signature.verify(message, signature, kp2.getPublicKey()));

        System.out.println("✓ 不同密钥测试通过");
    }
}
