package com.antherd.smcrypto;

import com.antherd.smcrypto.sm2.SignatureOptions;
import com.antherd.smcrypto.sm2.Sm2;
import org.junit.Test;

import javax.script.ScriptException;

import static org.junit.Assert.assertTrue;

public class Sm2InteropTest {

    @Test
    public void testSignatureInterop() throws ScriptException {
        // Issue #12中的密钥对
        String publicKey = "044310f0521b99a95fcb0a44f21fb0a77e9bda35516eb42208fc3a576ebc8cae93fa94834e22060abd8528483bad0ee010bda0bb32ef111bd8773a91aeb517f8db";
        String privateKey = "00d243de79a2741a3b37cc978add7ff39d7ac0d7c7f24ebadd95f3c3c071d4d5cd";
        String msg = "1234567890";

        // 测试1：默认签名（不使用DER）
        System.out.println("=== 测试1: 默认签名 ===");
        String signature1 = Sm2.doSignature(msg, privateKey);
        System.out.println("默认签名: " + signature1);
        System.out.println("签名长度: " + signature1.length());
        boolean verify1 = Sm2.doVerifySignature(msg, signature1, publicKey);
        System.out.println("默认验签: " + verify1);
        assertTrue("默认签名应该可以验签", verify1);

        // 测试2：DER格式签名
        System.out.println("\n=== 测试2: DER格式签名 ===");
        SignatureOptions derOptions = new SignatureOptions();
        derOptions.setDer(true);
        String signature2 = Sm2.doSignature(msg, privateKey, derOptions);
        System.out.println("DER签名: " + signature2);
        System.out.println("签名长度: " + signature2.length());
        boolean verify2 = Sm2.doVerifySignature(msg, signature2, publicKey, derOptions);
        System.out.println("DER验签: " + verify2);
        assertTrue("DER签名应该可以验签", verify2);

        // 测试3：Issue中的Java签名（DER格式）
        System.out.println("\n=== 测试3: Issue中的签名 ===");
        String issueSignature = "3044022010ba478cbc88d381b9f9e645e6572ae50878f2ce46eccfcfff753ef7231b834502204511da8360a98384294c3e8893ea2276e18ccc0b954c50671ade8c613a2091b2";
        System.out.println("Issue签名: " + issueSignature);
        System.out.println("签名长度: " + issueSignature.length());

        // 尝试用DER选项验签
        boolean verify3 = Sm2.doVerifySignature(msg, issueSignature, publicKey, derOptions);
        System.out.println("用DER选项验签: " + verify3);

        // 分析：Issue中的签名长度136，DER格式。JS版本可能默认不使用DER
        System.out.println("\n=== 结论 ===");
        System.out.println("Java默认签名长度: " + signature1.length() + " (RS拼接)");
        System.out.println("Java DER签名长度: " + signature2.length() + " (DER编码)");
        System.out.println("Issue签名长度: " + issueSignature.length() + " (DER编码)");
        System.out.println("\nJava和JS互操作需要使用相同的签名格式！");
    }
}
