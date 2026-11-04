package com.antherd.smcrypto.sm3;

/**
 * SM3配置选项
 *
 * @author geyiwei
 */
public class Sm3Options {

    /**
     * 密钥（用于HMAC）
     */
    private String key;

    public Sm3Options() {
    }

    public Sm3Options(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Sm3Options{" +
                "key='" + (key != null ? "***" : "null") + '\'' +
                '}';
    }
}
