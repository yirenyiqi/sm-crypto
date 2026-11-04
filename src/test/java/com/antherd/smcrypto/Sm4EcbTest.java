package com.antherd.smcrypto;

import com.antherd.smcrypto.sm4.Sm4;
import com.antherd.smcrypto.sm4.Sm4Options;
import org.junit.Test;

import javax.script.ScriptException;

import static org.junit.Assert.*;

public class Sm4EcbTest {

    @Test
    public void testEcbMode() throws ScriptException {
        String msg = "hello world! 我是 antherd.";
        String key = "0123456789abcdeffedcba9876543210";

        // 默认模式就是ECB
        String encryptDefault = Sm4.encrypt(msg, key);
        System.out.println("默认加密（ECB）: " + encryptDefault);

        // 显式指定ECB模式
        Sm4Options ecbOptions = new Sm4Options();
        ecbOptions.setMode("ecb");
        String encryptEcb = Sm4.encrypt(msg, key, ecbOptions);
        System.out.println("ECB模式加密: " + encryptEcb);

        // 两种方式结果应该相同
        assertEquals("默认模式和ECB模式应该产生相同结果", encryptDefault, encryptEcb);

        // ECB解密
        String decryptEcb = Sm4.decrypt(encryptEcb, key, ecbOptions);
        System.out.println("ECB模式解密: " + decryptEcb);
        assertEquals("解密后应该得到原文", msg, decryptEcb);

        // ECB模式相同明文块加密后相同
        String msg2 = "hello world! hello world! ";
        String encrypt2 = Sm4.encrypt(msg2, key, ecbOptions);
        System.out.println("重复明文ECB加密: " + encrypt2);

        // ECB模式的特点：相同的明文块产生相同的密文块
        String firstBlock = encrypt2.substring(0, 32);
        String secondBlock = encrypt2.substring(32, 64);
        System.out.println("第一块: " + firstBlock);
        System.out.println("第二块: " + secondBlock);
        // ECB模式相同明文块会产生相同密文（这是ECB的特征，也是其安全性弱点）
    }

    @Test
    public void testEcbVsCbc() throws ScriptException {
        String msg = "hello world! 我是 antherd.";
        String key = "0123456789abcdeffedcba9876543210";

        // ECB模式
        Sm4Options ecbOptions = new Sm4Options();
        ecbOptions.setMode("ecb");
        String ecbResult = Sm4.encrypt(msg, key, ecbOptions);
        System.out.println("ECB结果: " + ecbResult);

        // CBC模式
        Sm4Options cbcOptions = new Sm4Options();
        cbcOptions.setMode("cbc");
        cbcOptions.setIv("fedcba98765432100123456789abcdef");
        String cbcResult = Sm4.encrypt(msg, key, cbcOptions);
        System.out.println("CBC结果: " + cbcResult);

        // ECB和CBC结果应该不同
        assertNotEquals("ECB和CBC模式应该产生不同密文", ecbResult, cbcResult);
    }
}
