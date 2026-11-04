package com.antherd.smcrypto;

import com.antherd.smcrypto.sm2.Point;
import com.antherd.smcrypto.sm2.PointPoolManager;
import com.antherd.smcrypto.sm2.SignatureOptions;
import com.antherd.smcrypto.sm2.Sm2;
import org.junit.Test;

import javax.script.ScriptException;
import java.util.Queue;

import static org.junit.Assert.*;

public class PointPoolManagerTest {

    @Test
    public void testDefaultPoolInitialization() {
        int initialSize = PointPoolManager.getDefaultPoolSize();
        assertTrue("Default pool should be initialized with points", initialSize > 0);
    }

    @Test
    public void testSignatureWithDefaultPool() throws ScriptException {
        String msg = "performance test";
        String privateKey = "00d243de79a2741a3b37cc978add7ff39d7ac0d7c7f24ebadd95f3c3c071d4d5cd";
        String publicKey = "044310f0521b99a95fcb0a44f21fb0a77e9bda35516eb42208fc3a576ebc8cae93fa94834e22060abd8528483bad0ee010bda0bb32ef111bd8773a91aeb517f8db";

        // 使用默认点池
        SignatureOptions options = new SignatureOptions();
        options.setPointPool(PointPoolManager.getDefaultPool());

        long start = System.currentTimeMillis();
        String signature = Sm2.doSignature(msg, privateKey, options);
        long duration = System.currentTimeMillis() - start;

        System.out.println("使用点池签名耗时: " + duration + "ms");
        assertNotNull(signature);
        assertTrue(signature.length() > 0);

        // 验签
        boolean result = Sm2.doVerifySignature(msg, signature, publicKey);
        assertTrue("签名验签应该通过", result);
    }

    @Test
    public void testPerformanceComparison() throws ScriptException {
        String msg = "performance comparison";
        String privateKey = "00d243de79a2741a3b37cc978add7ff39d7ac0d7c7f24ebadd95f3c3c071d4d5cd";

        // 不使用点池
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Sm2.doSignature(msg, privateKey);
        }
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("不使用点池 10次签名: " + time1 + "ms");

        // 使用点池
        SignatureOptions options = new SignatureOptions();
        options.setPointPool(PointPoolManager.getDefaultPool());

        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Sm2.doSignature(msg, privateKey, options);
        }
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("使用点池 10次签名: " + time2 + "ms");

        double improvement = (double) time1 / time2;
        System.out.println("性能提升: " + String.format("%.2f", improvement) + "倍");

        assertTrue("使用点池应该更快", time2 < time1);
    }

    @Test
    public void testCreateCustomPool() throws ScriptException {
        int customSize = 50;
        Queue<Point> customPool = PointPoolManager.createPool(customSize);

        assertEquals("自定义点池大小应该正确", customSize, customPool.size());
    }

    @Test
    public void testRefillPool() throws ScriptException {
        Queue<Point> pool = PointPoolManager.createPool(10);
        assertEquals(10, pool.size());

        // 移除一些点
        for (int i = 0; i < 5; i++) {
            pool.poll();
        }
        assertEquals(5, pool.size());

        // 补充到20
        PointPoolManager.refillPool(pool, 20);
        assertEquals(20, pool.size());
    }
}
