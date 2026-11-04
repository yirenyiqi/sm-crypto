# SM2性能优化指南

## 性能问题分析

Issue #25提到："sm2加解密效率低下，需要几秒才能完成"

SM2基于JavaScript引擎（Nashorn）执行加密算法，相比原生实现确实会有性能开销。以下是优化建议。

## 优化方案

### 1. 使用预生成的椭圆曲线点（最有效）

**问题**：每次签名都需要生成椭圆曲线点，耗时较长。

**解决方案**：预先生成点池，重复使用。

```java
// 预生成椭圆曲线点池
Queue<Point> pointPool = new LinkedList<>();
for (int i = 0; i < 100; i++) {
    pointPool.add(Sm2.getPoint());
}

// 签名时使用点池
SignatureOptions options = new SignatureOptions();
options.setPointPool(pointPool);
String signature = Sm2.doSignature(msg, privateKey, options);
```

**性能提升**：可将签名时间从数百毫秒降低到数十毫秒。

### 2. 提供公钥避免推导（中等效果）

使用SM3杂凑时，如果提供公钥可以避免重新推导，提升性能。

```java
SignatureOptions options = new SignatureOptions();
options.setHash(true);
options.setPublicKey(publicKey);  // 提供公钥避免推导
String signature = Sm2.doSignature(msg, privateKey, options);
```

### 3. 根据场景选择合适的选项

不同选项组合的性能差异：

| 配置 | 耗时（相对） | 适用场景 |
|------|-------------|---------|
| 纯签名 + 预生成点池 | 最快 | 高性能场景 |
| 纯签名 + 生成点 | 较快 | 一般场景 |
| DER编解码 | 中等 | 需要标准格式 |
| SM3杂凑（无公钥） | 较慢 | 完整SM2签名 |
| SM3杂凑（有公钥） | 中等 | 完整SM2签名 + 优化 |

### 4. 升级到更新的JDK（推荐JDK 17+）

较新的JDK版本对Nashorn引擎有性能优化。如果使用JDK 15+，确保添加了Nashorn依赖：

```xml
<dependency>
    <groupId>org.openjdk.nashorn</groupId>
    <artifactId>nashorn-core</artifactId>
    <version>15.4</version>
</dependency>
```

### 5. 批量操作时复用引擎

当前实现已经复用了ScriptEngine实例（静态初始化），无需额外优化。

### 6. 考虑使用原生实现（终极方案）

如果性能要求极高，可以考虑：

1. **使用Bouncy Castle库**：纯Java实现的国密算法
   ```xml
   <dependency>
       <groupId>org.bouncycastle</groupId>
       <artifactId>bcprov-jdk15on</artifactId>
       <version>1.70</version>
   </dependency>
   ```

2. **使用GmSSL**：C语言实现，性能最优
   - 通过JNI调用
   - 性能可提升10-100倍

## 性能测试示例

```java
public class Sm2PerformanceTest {
    
    @Test
    public void testPerformance() throws ScriptException {
        String msg = "hello world";
        String privateKey = "...";
        String publicKey = "...";
        
        // 方案1：无优化
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Sm2.doSignature(msg, privateKey);
        }
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("无优化 100次签名: " + time1 + "ms");
        
        // 方案2：使用点池
        Queue<Point> pointPool = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            pointPool.add(Sm2.getPoint());
        }
        
        SignatureOptions options = new SignatureOptions();
        options.setPointPool(pointPool);
        
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Sm2.doSignature(msg, privateKey, options);
        }
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("使用点池 100次签名: " + time2 + "ms");
        System.out.println("性能提升: " + (time1 * 100.0 / time2) + "%");
    }
}
```

## 总结

对于Issue #25的"需要几秒才能完成"问题：

1. **立即见效**：使用预生成的椭圆曲线点池
2. **中期优化**：根据实际需求选择合适的签名选项组合
3. **长期方案**：如果性能仍不满足，考虑切换到原生实现（Bouncy Castle或GmSSL）

大多数场景下，使用点池优化后性能应该可以接受（单次签名10-50ms级别）。
