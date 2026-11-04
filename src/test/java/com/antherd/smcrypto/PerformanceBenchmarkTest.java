package com.antherd.smcrypto;

import com.antherd.smcrypto.core.Hex;
import com.antherd.smcrypto.core.SM3Digest;
import com.antherd.smcrypto.core.SM4Engine;
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

        // 预热
        for (int i = 0; i < 100; i++) {
            digest.digestHex(input);
        }

        // 纯Java实现
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

        // 预热
        for (int i = 0; i < 100; i++) {
            SM4Engine.encryptEcb(plaintext, key);
        }

        // 纯Java实现
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

        // 预热
        for (int i = 0; i < 100; i++) {
            SM4Engine.encryptCbc(plaintext, key, iv);
        }

        // 纯Java实现
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
        // 旧版JavaScript引擎实现
        String msg = "测试消息";
        String privateKey = "9ee0a3ba42e6dd4acbcc9da201c678024bf1670d6eee5567faa2cd9cc9ea989c";

        // 预热
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
}
