package com.antherd.smcrypto;

import com.antherd.smcrypto.core.*;
import com.antherd.smcrypto.sm2.Sm2;
import org.junit.Test;

/**
 * 性能对比测试：纯Java实现 vs JavaScript引擎调用
 */
public class PerformanceBenchmarkTest {

    private static final int ITERATIONS = 1000;

    @Test
    public void benchmarkSM3() {
        SM3Digest digest = new SM3Digest();
        String input = "这是一段测试数据，用于性能对比测试";

        for (int i = 0; i < 100; i++) {
            digest.digestHex(input);
        }

        long javaStart = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            digest.digestHex(input);
        }
        long javaTime = System.currentTimeMillis() - javaStart;

        System.out.println("=== SM3性能对比 ===");
        System.out.println("纯Java实现: " + ITERATIONS + "次耗时 " + javaTime + "ms");
        System.out.println("平均每次: " + (javaTime * 1.0 / ITERATIONS) + "ms");
    }

    @Test
    public void benchmarkSM4Ecb() {
        String key = "0123456789abcdeffedcba9876543210";
        String plaintext = "hello world! 我是测试数据，用于性能对比";

        for (int i = 0; i < 100; i++) {
            SM4Engine.encryptEcb(plaintext, key);
        }

        long javaStart = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            SM4Engine.encryptEcb(plaintext, key);
        }
        long javaTime = System.currentTimeMillis() - javaStart;

        System.out.println("=== SM4 ECB性能对比 ===");
        System.out.println("纯Java实现: " + ITERATIONS + "次耗时 " + javaTime + "ms");
        System.out.println("平均每次: " + (javaTime * 1.0 / ITERATIONS) + "ms");
    }

    @Test
    public void benchmarkSM4Cbc() {
        String key = "0123456789abcdeffedcba9876543210";
        String iv = "fedcba98765432100123456789abcdef";
        String plaintext = "hello world! 我是测试数据，用于性能对比";

        for (int i = 0; i < 100; i++) {
            SM4Engine.encryptCbc(plaintext, key, iv);
        }

        long javaStart = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            SM4Engine.encryptCbc(plaintext, key, iv);
        }
        long javaTime = System.currentTimeMillis() - javaStart;

        System.out.println("=== SM4 CBC性能对比 ===");
        System.out.println("纯Java实现: " + ITERATIONS + "次耗时 " + javaTime + "ms");
        System.out.println("平均每次: " + (javaTime * 1.0 / ITERATIONS) + "ms");
    }

    @Test
    public void benchmarkSM2SignatureOld() throws Exception {
        String msg = "测试消息";
        String privateKey = "9ee0a3ba42e6dd4acbcc9da201c678024bf1670d6eee5567faa2cd9cc9ea989c";

        for (int i = 0; i < 10; i++) {
            Sm2.doSignature(msg, privateKey);
        }

        long oldStart = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Sm2.doSignature(msg, privateKey);
        }
        long oldTime = System.currentTimeMillis() - oldStart;

        System.out.println("=== SM2签名性能（旧实现） ===");
        System.out.println("JavaScript引擎: 100次耗时 " + oldTime + "ms");
        System.out.println("平均每次: " + (oldTime * 1.0 / 100) + "ms");
        System.out.println("注：旧实现通过Nashorn调用JavaScript，存在跨语言开销");
    }

    @Test
    public void benchmarkSM2SignatureNew() {
        String msg = "测试消息";
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();

        for (int i = 0; i < 100; i++) {
            SM2Signature.sign(msg, kp.getPrivateKey());
        }

        long signStart = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            SM2Signature.sign(msg, kp.getPrivateKey());
        }
        long signTime = System.currentTimeMillis() - signStart;

        System.out.println("=== SM2签名性能（纯Java） ===");
        System.out.println("纯Java: " + ITERATIONS + "次耗时 " + signTime + "ms");
        System.out.println("平均: " + (signTime * 1.0 / ITERATIONS) + "ms");

        String signature = SM2Signature.sign(msg, kp.getPrivateKey());

        for (int i = 0; i < 100; i++) {
            SM2Signature.verify(msg, signature, kp.getPublicKey());
        }

        long verifyStart = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            SM2Signature.verify(msg, signature, kp.getPublicKey());
        }
        long verifyTime = System.currentTimeMillis() - verifyStart;

        System.out.println("=== SM2验签性能（纯Java） ===");
        System.out.println("纯Java: " + ITERATIONS + "次耗时 " + verifyTime + "ms");
        System.out.println("平均: " + (verifyTime * 1.0 / ITERATIONS) + "ms");
    }

    @Test
    public void benchmarkSM2Encrypt() {
        String plaintext = "测试SM2加密性能";
        SM2KeyPair kp = SM2KeyPair.generateKeyPair();

        for (int i = 0; i < 50; i++) {
            SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);
        }

        long encStart = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);
        }
        long encTime = System.currentTimeMillis() - encStart;

        System.out.println("=== SM2加密性能 ===");
        System.out.println("纯Java: 100次耗时 " + encTime + "ms");
        System.out.println("平均: " + (encTime * 1.0 / 100) + "ms");

        String ciphertext = SM2Engine.encrypt(plaintext, kp.getPublicKey(), SM2Engine.C1C3C2);

        for (int i = 0; i < 50; i++) {
            SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C3C2);
        }

        long decStart = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            SM2Engine.decrypt(ciphertext, kp.getPrivateKey(), SM2Engine.C1C3C2);
        }
        long decTime = System.currentTimeMillis() - decStart;

        System.out.println("=== SM2解密性能 ===");
        System.out.println("纯Java: 100次耗时 " + decTime + "ms");
        System.out.println("平均: " + (decTime * 1.0 / 100) + "ms");
    }
}
