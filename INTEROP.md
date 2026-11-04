# Java和JavaScript版本互操作指南

## 问题背景

sm-crypto的Java版本基于JS版本封装，但在签名验签互操作时需要注意格式一致性。

## SM2签名格式

SM2签名有两种常见格式：

1. **RS拼接格式**（默认）：128字符十六进制字符串
   - 直接将R和S拼接：`R(64字符) + S(64字符)`
   
2. **DER编码格式**：长度可变（通常136-144字符）
   - ASN.1 DER编码：`30 [长度] 02 [R长度] [R] 02 [S长度] [S]`

## Java和JS互操作的正确方式

### 场景1：Java签名 → JS验签

**Java端：**
```java
// 使用DER格式签名
SignatureOptions options = new SignatureOptions();
options.setDer(true);
String signature = Sm2.doSignature(msg, privateKey, options);
```

**JS端：**
```javascript
// 使用der选项验签
const verifyResult = sm2.doVerifySignature(msg, signature, publicKey, {
  der: true
});
```

### 场景2：JS签名 → Java验签

**JS端：**
```javascript
// 使用der格式签名
const signature = sm2.doSignature(msg, privateKey, {
  der: true
});
```

**Java端：**
```java
// 使用DER选项验签
SignatureOptions options = new SignatureOptions();
options.setDer(true);
boolean verifyResult = Sm2.doVerifySignature(msg, signature, publicKey, options);
```

### 场景3：不使用DER（仅同语言内使用）

**Java端：**
```java
// 默认格式（RS拼接）
String signature = Sm2.doSignature(msg, privateKey);
boolean verifyResult = Sm2.doVerifySignature(msg, signature, publicKey);
```

**JS端：**
```javascript
// 默认格式（RS拼接）
const signature = sm2.doSignature(msg, privateKey);
const verifyResult = sm2.doVerifySignature(msg, signature, publicKey);
```

## 重要提示

⚠️ **跨语言互操作时，必须使用相同的签名格式！**

- 推荐使用DER格式（`der: true`）进行跨语言互操作
- DER是标准格式，具有更好的兼容性
- 默认的RS拼接格式仅推荐在同一语言内使用

## Issue #12的问题分析

Issue #12中：
```
publickey: 044310f0521b99a95fcb0a44f21fb0a77e9bda35516eb42208fc3a576ebc8cae93fa94834e22060abd8528483bad0ee010bda0bb32ef111bd8773a91aeb517f8db
privatekey: 00d243de79a2741a3b37cc978add7ff39d7ac0d7c7f24ebadd95f3c3c071d4d5cd
原始串: 1234567890

Java版本签名：3044022010ba478cbc88d381b9f9e645e6572ae50878f2ce46eccfcfff753ef7231b834502204511da8360a98384294c3e8893ea2276e18ccc0b954c50671ade8c613a2091b2
JS版本验签不通过
```

**原因**：Java签名使用了DER格式（140字符），但JS验签时未指定`der: true`选项。

**解决方案**：
```javascript
// JS端验签时添加der选项
const verifyResult = sm2.doVerifySignature(
  '1234567890',
  '3044022010ba478cbc88d381b9f9e645e6572ae50878f2ce46eccfcfff753ef7231b834502204511da8360a98384294c3e8893ea2276e18ccc0b954c50671ade8c613a2091b2',
  '044310f0521b99a95fcb0a44f21fb0a77e9bda35516eb42208fc3a576ebc8cae93fa94834e22060abd8528483bad0ee010bda0bb32ef111bd8773a91aeb517f8db',
  { der: true }  // 必须指定der选项
);
```

## 其他选项

签名验签还支持其他选项，需要保持一致：

```java
SignatureOptions options = new SignatureOptions();
options.setDer(true);           // DER编码
options.setHash(true);          // 启用SM3杂凑
options.setPublicKey(publicKey); // 提供公钥（优化性能）
options.setUserId("custom");    // 自定义userId
```

对应的JS选项：
```javascript
{
  der: true,
  hash: true,
  publicKey: '...',
  userId: 'custom'
}
```
