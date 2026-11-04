package com.antherd.smcrypto;

import com.antherd.smcrypto.core.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SM2密钥对测试
 */
public class SM2KeyPairTest {

    @Test
    public void testGenerateKeyPair() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();

        assertNotNull("私钥不应为空", kp.getPrivateKey());
        assertNotNull("公钥不应为空", kp.getPublicKey());

        assertEquals("私钥应为64位16进制", 64, kp.getPrivateKey().length());
        assertEquals("公钥应为130位16进制（未压缩）", 130, kp.getPublicKey().length());
        assertTrue("公钥应以04开头", kp.getPublicKey().startsWith("04"));

        System.out.println("生成密钥对:");
        System.out.println("私钥: " + kp.getPrivateKey());
        System.out.println("公钥: " + kp.getPublicKey());
    }

    @Test
    public void testGetPublicKeyFromPrivate() {
        String privateKey = "9ee0a3ba42e6dd4acbcc9da201c678024bf1670d6eee5567faa2cd9cc9ea989c";
        String publicKey = SM2KeyPair.getPublicKeyFromPrivate(privateKey);

        assertNotNull("公钥不应为空", publicKey);
        assertEquals("公钥长度", 130, publicKey.length());

        System.out.println("从私钥计算公钥:");
        System.out.println("公钥: " + publicKey);
    }

    @Test
    public void testVerifyPublicKey() {
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();
        String publicKey = kp.getPublicKey();

        assertTrue("有效公钥应验证通过", SM2KeyPair.verifyPublicKey(publicKey));

        // 无效公钥
        assertFalse("空公钥", SM2KeyPair.verifyPublicKey(""));
        assertFalse("无穷远点", SM2KeyPair.verifyPublicKey("00"));
    }

    @Test
    public void testCompressPublicKey() {
        String uncompressed = "04" +
            "32c4ae2c1f1981195f9904466a39c9948fe30bbff2660be1715a4589334c74c7" +
            "bc3736a2f4f6779c59bdcee36b692153d0a9877cc62a474002df32e52139f0a0";

        String compressed = SM2KeyPair.compressPublicKey(uncompressed);

        assertNotNull("压缩公钥不应为空", compressed);
        assertEquals("压缩公钥长度", 66, compressed.length());
        assertTrue("压缩公钥应以02或03开头",
                  compressed.startsWith("02") || compressed.startsWith("03"));

        System.out.println("未压缩公钥: " + uncompressed);
        System.out.println("压缩公钥: " + compressed);

        // 验证压缩和未压缩公钥都有效
        assertTrue("未压缩公钥有效", SM2KeyPair.verifyPublicKey(uncompressed));
        assertTrue("压缩公钥有效", SM2KeyPair.verifyPublicKey(compressed));
    }

    @Test
    public void testMultipleKeyPairs() {
        // 生成多个密钥对，确保随机性
        SM2KeyPair kp1 = SM2KeyPair.generateKeyPair();
        SM2KeyPair kp2 = SM2KeyPair.generateKeyPair();

        assertNotEquals("不同的密钥对私钥应不同",
                       kp1.getPrivateKey(), kp2.getPrivateKey());
        assertNotEquals("不同的密钥对公钥应不同",
                       kp1.getPublicKey(), kp2.getPublicKey());

        // 验证都有效
        assertTrue(SM2KeyPair.verifyPublicKey(kp1.getPublicKey()));
        assertTrue(SM2KeyPair.verifyPublicKey(kp2.getPublicKey()));
    }
}
