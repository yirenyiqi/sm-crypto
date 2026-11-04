# sm-crypto

国密算法sm2、sm3和sm4的java版。基于js版本进行封装，无缝兼容js版公私钥加解密。

> PS: js版：[https://github.com/JuneAndGreen/sm-crypto](https://github.com/JuneAndGreen/sm-crypto)
>
> PS: 小程序版：[https://github.com/wechat-miniprogram/sm-crypto](https://github.com/wechat-miniprogram/sm-crypto)

## 如何使用
### 如何引入依赖
如果需要使用已发布的版本，在`dependencies`中添加如下依赖
```xml
<dependency>
    <groupId>com.antherd</groupId>
    <artifactId>sm-crypto</artifactId>
    <version>0.3.2.1</version>
</dependency>
```

## sm2

### 获取密钥对

```java
Keypair keypair = Sm2.generateKeyPairHex();
String privateKey = keypair.getPrivateKey(); // 公钥
String publicKey = keypair.getPublicKey(); // 私钥
```

### 加密解密

```java 
// cipherMode 1 - C1C3C2，0 - C1C2C3，默认为1
String encryptData = Sm2.doEncrypt(msg, publicKey); // 加密结果
String decryptData = Sm2.doDecrypt(encryptData, privateKey); // 解密结果
```

### 签名验签

> ps：理论上来说，只做纯签名是最快的。

```java
// 纯签名 + 生成椭圆曲线点
String sigValueHex = Sm2.doSignature(msg, privateKey); // 签名
boolean verifyResult = Sm2.doVerifySignature(msg, sigValueHex, publicKey); // 验签结果

// 使用预生成点池优化性能（推荐）
SignatureOptions signatureOptions2 = new SignatureOptions();
signatureOptions2.setPointPool(PointPoolManager.getDefaultPool()); // 使用默认点池，显著提升签名速度
String sigValueHex2 = Sm2.doSignature(msg, privateKey, signatureOptions2);

// 或手动创建点池
Queue<Point> pointPool = new LinkedList(Arrays.asList(Sm2.getPoint(), Sm2.getPoint(), Sm2.getPoint(), Sm2.getPoint()));
SignatureOptions signatureOptions3 = new SignatureOptions();
signatureOptions3.setPointPool(pointPool);
String sigValueHex2 = Sm2.doSignature(msg, privateKey, signatureOptions2);
boolean verifyResult2 = Sm2.doVerifySignature(msg, sigValueHex2, publicKey); // 验签结果

// 纯签名 + 生成椭圆曲线点 + der编解码
SignatureOptions signatureOptions3 = new SignatureOptions();
signatureOptions3.setDer(true);
String sigValueHex3 = Sm2.doSignature(msg, privateKey, signatureOptions3); // 签名
boolean verifyResult3 = Sm2.doVerifySignature(msg, sigValueHex3, publicKey, signatureOptions3); // 验签结果

// 纯签名 + 生成椭圆曲线点 + sm3杂凑
SignatureOptions signatureOptions4 = new SignatureOptions();
signatureOptions4.setHash(true);
String sigValueHex4 = Sm2.doSignature(msg, privateKey, signatureOptions4); // 签名
boolean verifyResult4 = Sm2.doVerifySignature(msg, sigValueHex4, publicKey, signatureOptions4); // 验签结果

// 纯签名 + 生成椭圆曲线点 + sm3杂凑（不做公钥推导）
SignatureOptions signatureOptions5 = new SignatureOptions();
signatureOptions5.setHash(true);
signatureOptions5.setPublicKey(publicKey); // 传入公钥的话，可以去掉sm3杂凑中推导公钥的过程，速度会比纯签名 + 生成椭圆曲线点 + sm3杂凑快
String sigValueHex5 = Sm2.doSignature(msg, privateKey, signatureOptions5); // 签名
boolean verifyResult5 = Sm2.doVerifySignature(msg, sigValueHex5, publicKey, signatureOptions5); // 验签结果

// 纯签名 + 生成椭圆曲线点 + sm3杂凑 + 不做公钥推 + 添加 userId（长度小于 8192）
// 默认 userId 值为 1234567812345678
SignatureOptions signatureOptions6 = new SignatureOptions();
signatureOptions6.setHash(true);
signatureOptions6.setPublicKey(publicKey);
signatureOptions6.setUserId("testUserId");
String sigValueHex6 = Sm2.doSignature(msg, privateKey, signatureOptions6); // 签名
boolean verifyResult6 = Sm2.doVerifySignature(msg, sigValueHex6, publicKey, signatureOptions6); // 验签结果
```

### 获取椭圆曲线点

```java
Point point = Sm2.getPoint(); // 获取一个椭圆曲线点，可在sm2签名时传入
```

## sm3

```java
String hashData = Sm3.sm3("abc"); // 杂凑
```

## sm4

### 加密

```java
String msg = "hello world! 我是 antherd.";
String key = "0123456789abcdeffedcba9876543210"; // 16 进制字符串，要求为 128 比特

String encryptData1 = Sm4.encrypt(msg, key); // 加密，默认使用 pkcs#5 填充，输出16进制字符串

Sm4Options sm4Options2 = new Sm4Options();
sm4Options2.setPadding("none");
String encryptData2 = Sm4.encrypt(msg, key, sm4Options2); // 加密，不使用 padding，输出16进制字符串

Sm4Options sm4Options3 = new Sm4Options();
sm4Options3.setPadding("none");
byte[] encryptData3 = Sm4.hexToBytes(Sm4.encrypt(msg, key, sm4Options3)); // 加密，不使用 padding，输出转为字节数组

Sm4Options sm4Options4 = new Sm4Options();
sm4Options4.setMode("cbc");
sm4Options4.setIv("fedcba98765432100123456789abcdef");
String encryptData4 = Sm4.encrypt(msg, key, sm4Options4); // 加密，cbc 模式，输出16进制字符串
```

### 解密

```java
String encryptData = "0e395deb10f6e8a17e17823e1fd9bd98a1bff1df508b5b8a1efb79ec633d1bb129432ac1b74972dbe97bab04f024e89c"; // 加密后的 16 进制字符串
String key = "0123456789abcdeffedcba9876543210"; // 16 进制字符串，要求为 128 比特

String decryptData5 = Sm4.decrypt(encryptData, key); // 解密，默认使用 pkcs#5 填充，输出 utf8 字符串

Sm4Options sm4Options6 = new Sm4Options();
sm4Options6.setPadding("none");
String decryptData6 = Sm4.decrypt(encryptData, key, sm4Options6); // 解密，不使用 padding，输出 utf8 字符串

Sm4Options sm4Options7 = new Sm4Options();
sm4Options7.setPadding("none");
byte[] decryptData7 = Sm4.utf8ToArray(Sm4.decrypt(encryptData, key, sm4Options7)); // 解密，不使用 padding，输出转为字节数组

Sm4Options sm4Options8 = new Sm4Options();
sm4Options8.setMode("cbc");
sm4Options8.setIv("fedcba98765432100123456789abcdef");
String decryptData8 = Sm4.decrypt(encryptData, key, sm4Options8); // 解密，cbc 模式，输出 utf8 字符串
```

## 协议

[MIT](https://github.com/antherd/sm-crypto/blob/master/LICENSE)
