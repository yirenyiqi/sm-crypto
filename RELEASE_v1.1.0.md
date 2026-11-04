# Release v1.1.0 - SM2/SM3/SM4纯Java实现

发布日期：2026-11-04

## 🎉 重大更新

本版本完成SM2/SM3/SM4国密算法的纯Java实现，**SM2性能提升11.9倍**。

## ✨ 新增特性

### SM3哈希算法（纯Java）
- 完整实现国密GM/T 0004-2012标准
- 支持HMAC-SM3
- 性能：<0.001ms/次（微秒级）

### SM4对称加密（纯Java）
- 完整实现国密GM/T 0002-2012标准
- 支持ECB/CBC模式
- PKCS#7自动填充
- 性能：0.002-0.004ms/次（微秒级）

### SM2椭圆曲线（纯Java）✨
- 完整实现国密GM/T 0003-2012标准
- 密钥对生成与管理
- 加密解密（支持C1C2C3/C1C3C2模式）
- 签名验签
- 公钥压缩/解压缩
- KDF密钥派生函数

## 🚀 性能提升

| 算法 | 旧版本 | v1.1.0 | 提升倍数 |
|-----|-------|--------|---------|
| **SM2签名** | 23.69ms | **1.99ms** | **11.9倍** ⚡️ |
| **SM2验签** | ~24ms | **1.97ms** | **12.2倍** ⚡️ |
| SM2加密 | - | 2.29ms | - |
| SM2解密 | - | 0.97ms | - |
| SM3哈希 | - | <0.001ms | - |
| SM4 ECB | - | 0.002ms | - |
| SM4 CBC | - | 0.004ms | - |

**核心优化**：
- 使用射影坐标优化椭圆曲线点运算
- 移除JavaScript引擎调用开销
- 纯JDK标准库实现，无第三方依赖

## 🐛 问题修复

- **#22** 修复SM4 CBC模式不同IV产生相同密文的问题
- **#25** 彻底解决SM2性能低下问题（提升11.9倍）

## 📦 代码统计

- 核心算法：9个文件，1724行
- 测试代码：7个文件，572行
- 总计新增：3110行

## ✅ 兼容性

- 与JuneAndGreen/sm-crypto JavaScript版本100%兼容
- JDK 8+ 全面支持
- 所有测试通过（48个测试，0失败）
- 无破坏性变更

## 📚 技术亮点

1. **椭圆曲线优化**
   - 射影坐标系统减少模逆运算
   - 高效点乘算法
   - 标准曲线参数

2. **架构设计**
   - 核心算法层：`com.antherd.smcrypto.core.*`
   - API适配层：`com.antherd.smcrypto.sm*`
   - 清晰的分层架构

3. **完整测试覆盖**
   - 单元测试（48个）
   - 互操作测试
   - 性能基准测试
   - 边界条件测试

## 🔄 迁移指南

本版本向后兼容，无需修改现有代码。新的纯Java实现通过现有API自动使用。

```java
// 现有代码无需修改，性能自动提升
String hash = Sm3.sm3("hello");
String ciphertext = Sm4.encrypt("data", key);
String signature = Sm2.doSignature(msg, privateKey);
```

## 📝 依赖变更

- 无新增依赖
- 建议：未来版本可移除Nashorn依赖

## 🙏 致谢

感谢所有贡献者和Issue反馈者。

## 📖 文档

- [REFACTOR_REPORT.md](REFACTOR_REPORT.md) - 详细技术报告
- [FINAL_SUMMARY.md](FINAL_SUMMARY.md) - 完整项目总结

---

**完整变更日志**：https://github.com/yirenyiqi/sm-crypto/compare/v1.0.0...v1.1.0
