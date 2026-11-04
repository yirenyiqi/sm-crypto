package com.antherd.smcrypto;

import com.antherd.smcrypto.sm4.Sm4;
import com.antherd.smcrypto.sm4.Sm4Options;
import org.junit.Test;

import javax.script.ScriptException;

import static org.junit.Assert.assertNotEquals;

public class Sm4IvTest {

    @Test
    public void testDifferentIvProduceDifferentCiphertext() throws ScriptException {
        String msg = "hello world! 我是 antherd.";
        String key = "0123456789abcdeffedcba9876543210";

        // 使用第一个IV加密
        Sm4Options options1 = new Sm4Options();
        options1.setMode("cbc");
        options1.setIv("fedcba98765432100123456789abcdef");
        String encryptData1 = Sm4.encrypt(msg, key, options1);
        System.out.println("IV1 result: " + encryptData1);

        // 使用第二个IV加密
        Sm4Options options2 = new Sm4Options();
        options2.setMode("cbc");
        options2.setIv("0123456789abcdeffedcba9876543210");
        String encryptData2 = Sm4.encrypt(msg, key, options2);
        System.out.println("IV2 result: " + encryptData2);

        // 不同IV应该产生不同的密文
        assertNotEquals("不同的IV应该产生不同的密文", encryptData1, encryptData2);
    }
}
