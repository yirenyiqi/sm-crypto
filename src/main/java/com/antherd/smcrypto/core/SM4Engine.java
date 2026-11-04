package com.antherd.smcrypto.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * SM4分组密码算法 - 纯Java实现
 * 国密算法标准: GM/T 0002-2012
 *
 * @author geyiwei
 */
public class SM4Engine {
    private static final int ENCRYPT = 1;
    private static final int DECRYPT = 0;
    private static final int ROUND = 32;
    private static final int BLOCK_SIZE = 16;

    // S盒
    private static final int[] SBOX = {
        0xd6, 0x90, 0xe9, 0xfe, 0xcc, 0xe1, 0x3d, 0xb7, 0x16, 0xb6, 0x14, 0xc2, 0x28, 0xfb, 0x2c, 0x05,
        0x2b, 0x67, 0x9a, 0x76, 0x2a, 0xbe, 0x04, 0xc3, 0xaa, 0x44, 0x13, 0x26, 0x49, 0x86, 0x06, 0x99,
        0x9c, 0x42, 0x50, 0xf4, 0x91, 0xef, 0x98, 0x7a, 0x33, 0x54, 0x0b, 0x43, 0xed, 0xcf, 0xac, 0x62,
        0xe4, 0xb3, 0x1c, 0xa9, 0xc9, 0x08, 0xe8, 0x95, 0x80, 0xdf, 0x94, 0xfa, 0x75, 0x8f, 0x3f, 0xa6,
        0x47, 0x07, 0xa7, 0xfc, 0xf3, 0x73, 0x17, 0xba, 0x83, 0x59, 0x3c, 0x19, 0xe6, 0x85, 0x4f, 0xa8,
        0x68, 0x6b, 0x81, 0xb2, 0x71, 0x64, 0xda, 0x8b, 0xf8, 0xeb, 0x0f, 0x4b, 0x70, 0x56, 0x9d, 0x35,
        0x1e, 0x24, 0x0e, 0x5e, 0x63, 0x58, 0xd1, 0xa2, 0x25, 0x22, 0x7c, 0x3b, 0x01, 0x21, 0x78, 0x87,
        0xd4, 0x00, 0x46, 0x57, 0x9f, 0xd3, 0x27, 0x52, 0x4c, 0x36, 0x02, 0xe7, 0xa0, 0xc4, 0xc8, 0x9e,
        0xea, 0xbf, 0x8a, 0xd2, 0x40, 0xc7, 0x38, 0xb5, 0xa3, 0xf7, 0xf2, 0xce, 0xf9, 0x61, 0x15, 0xa1,
        0xe0, 0xae, 0x5d, 0xa4, 0x9b, 0x34, 0x1a, 0x55, 0xad, 0x93, 0x32, 0x30, 0xf5, 0x8c, 0xb1, 0xe3,
        0x1d, 0xf6, 0xe2, 0x2e, 0x82, 0x66, 0xca, 0x60, 0xc0, 0x29, 0x23, 0xab, 0x0d, 0x53, 0x4e, 0x6f,
        0xd5, 0xdb, 0x37, 0x45, 0xde, 0xfd, 0x8e, 0x2f, 0x03, 0xff, 0x6a, 0x72, 0x6d, 0x6c, 0x5b, 0x51,
        0x8d, 0x1b, 0xaf, 0x92, 0xbb, 0xdd, 0xbc, 0x7f, 0x11, 0xd9, 0x5c, 0x41, 0x1f, 0x10, 0x5a, 0xd8,
        0x0a, 0xc1, 0x31, 0x88, 0xa5, 0xcd, 0x7b, 0xbd, 0x2d, 0x74, 0xd0, 0x12, 0xb8, 0xe5, 0xb4, 0xb0,
        0x89, 0x69, 0x97, 0x4a, 0x0c, 0x96, 0x77, 0x7e, 0x65, 0xb9, 0xf1, 0x09, 0xc5, 0x6e, 0xc6, 0x84,
        0x18, 0xf0, 0x7d, 0xec, 0x3a, 0xdc, 0x4d, 0x20, 0x79, 0xee, 0x5f, 0x3e, 0xd7, 0xcb, 0x39, 0x48
    };

    // 系统参数FK
    private static final int[] FK = {
        0xa3b1bac6, 0x56aa3350, 0x677d9197, 0xb27022dc
    };

    // 固定参数CK
    private static final int[] CK = {
        0x00070e15, 0x1c232a31, 0x383f464d, 0x545b6269,
        0x70777e85, 0x8c939aa1, 0xa8afb6bd, 0xc4cbd2d9,
        0xe0e7eef5, 0xfc030a11, 0x181f262d, 0x343b4249,
        0x50575e65, 0x6c737a81, 0x888f969d, 0xa4abb2b9,
        0xc0c7ced5, 0xdce3eaf1, 0xf8ff060d, 0x141b2229,
        0x30373e45, 0x4c535a61, 0x686f767d, 0x848b9299,
        0xa0a7aeb5, 0xbcc3cad1, 0xd8dfe6ed, 0xf4fb0209,
        0x10171e25, 0x2c333a41, 0x484f565d, 0x646b7279
    };

    /**
     * SM4加密 - ECB模式
     */
    public static String encryptEcb(String plaintext, String keyHex) {
        byte[] key = Hex.decode(keyHex);
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = processEcb(data, key, ENCRYPT);
        return Hex.encode(encrypted);
    }

    /**
     * SM4解密 - ECB模式
     */
    public static String decryptEcb(String cipherHex, String keyHex) {
        byte[] key = Hex.decode(keyHex);
        byte[] data = Hex.decode(cipherHex);
        byte[] decrypted = processEcb(data, key, DECRYPT);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * SM4加密 - CBC模式
     */
    public static String encryptCbc(String plaintext, String keyHex, String ivHex) {
        byte[] key = Hex.decode(keyHex);
        byte[] iv = Hex.decode(ivHex);
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = processCbc(data, key, iv, ENCRYPT);
        return Hex.encode(encrypted);
    }

    /**
     * SM4解密 - CBC模式
     */
    public static String decryptCbc(String cipherHex, String keyHex, String ivHex) {
        byte[] key = Hex.decode(keyHex);
        byte[] iv = Hex.decode(ivHex);
        byte[] data = Hex.decode(cipherHex);
        byte[] decrypted = processCbc(data, key, iv, DECRYPT);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * ECB模式处理
     */
    private static byte[] processEcb(byte[] input, byte[] key, int mode) {
        if (key.length != 16) {
            throw new IllegalArgumentException("Key must be 128 bits (16 bytes)");
        }

        // PKCS#7填充
        byte[] padded = mode == ENCRYPT ? pkcs7Pad(input) : input;

        // 生成轮密钥
        int[] roundKey = new int[ROUND];
        expandKey(key, roundKey, mode);

        // 分组处理
        int blockCount = padded.length / BLOCK_SIZE;
        byte[] output = new byte[padded.length];

        for (int i = 0; i < blockCount; i++) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(padded, i * BLOCK_SIZE, block, 0, BLOCK_SIZE);

            byte[] processed = processBlock(block, roundKey);
            System.arraycopy(processed, 0, output, i * BLOCK_SIZE, BLOCK_SIZE);
        }

        // 解密时去除填充
        return mode == DECRYPT ? pkcs7Unpad(output) : output;
    }

    /**
     * CBC模式处理
     */
    private static byte[] processCbc(byte[] input, byte[] key, byte[] iv, int mode) {
        if (key.length != 16) {
            throw new IllegalArgumentException("Key must be 128 bits (16 bytes)");
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("IV must be 128 bits (16 bytes)");
        }

        // PKCS#7填充
        byte[] padded = mode == ENCRYPT ? pkcs7Pad(input) : input;

        // 生成轮密钥
        int[] roundKey = new int[ROUND];
        expandKey(key, roundKey, mode);

        // 分组处理
        int blockCount = padded.length / BLOCK_SIZE;
        byte[] output = new byte[padded.length];
        byte[] lastVector = Arrays.copyOf(iv, BLOCK_SIZE);

        for (int i = 0; i < blockCount; i++) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(padded, i * BLOCK_SIZE, block, 0, BLOCK_SIZE);

            if (mode == ENCRYPT) {
                // 加密前异或IV
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    block[j] ^= lastVector[j];
                }

                byte[] processed = processBlock(block, roundKey);
                System.arraycopy(processed, 0, output, i * BLOCK_SIZE, BLOCK_SIZE);
                lastVector = processed;
            } else {
                // 解密
                byte[] inputCopy = Arrays.copyOf(block, BLOCK_SIZE);
                byte[] processed = processBlock(block, roundKey);

                // 解密后异或IV
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    processed[j] ^= lastVector[j];
                }

                System.arraycopy(processed, 0, output, i * BLOCK_SIZE, BLOCK_SIZE);
                lastVector = inputCopy;
            }
        }

        // 解密时去除填充
        return mode == DECRYPT ? pkcs7Unpad(output) : output;
    }

    /**
     * 处理一个16字节分组
     */
    private static byte[] processBlock(byte[] input, int[] roundKey) {
        int[] X = new int[4];

        // 字节数组转字数组
        for (int i = 0; i < 4; i++) {
            X[i] = bytesToInt(input, i * 4);
        }

        // 32轮迭代
        for (int r = 0; r < ROUND; r++) {
            int temp = X[1] ^ X[2] ^ X[3] ^ roundKey[r];
            X[0] ^= L1(byteSub(temp));

            // 循环移位
            int swap = X[0];
            X[0] = X[1];
            X[1] = X[2];
            X[2] = X[3];
            X[3] = swap;
        }

        // 反序变换
        byte[] output = new byte[BLOCK_SIZE];
        intToBytes(X[3], output, 0);
        intToBytes(X[2], output, 4);
        intToBytes(X[1], output, 8);
        intToBytes(X[0], output, 12);

        return output;
    }

    /**
     * 密钥扩展
     */
    private static void expandKey(byte[] key, int[] roundKey, int cryptFlag) {
        int[] MK = new int[4];
        int[] K = new int[4];

        // 字节数组转字数组
        for (int i = 0; i < 4; i++) {
            MK[i] = bytesToInt(key, i * 4);
        }

        // 与系统参数FK异或
        for (int i = 0; i < 4; i++) {
            K[i] = MK[i] ^ FK[i];
        }

        // 生成32个轮密钥
        for (int r = 0; r < ROUND; r++) {
            int temp = K[1] ^ K[2] ^ K[3] ^ CK[r];
            K[0] ^= L2(byteSub(temp));
            roundKey[r] = K[0];

            // 循环移位
            int swap = K[0];
            K[0] = K[1];
            K[1] = K[2];
            K[2] = K[3];
            K[3] = swap;
        }

        // 解密时反序轮密钥
        if (cryptFlag == DECRYPT) {
            for (int i = 0; i < 16; i++) {
                int temp = roundKey[i];
                roundKey[i] = roundKey[31 - i];
                roundKey[31 - i] = temp;
            }
        }
    }

    /**
     * 非线性变换
     */
    private static int byteSub(int a) {
        return (SBOX[(a >>> 24) & 0xff] & 0xff) << 24
             | (SBOX[(a >>> 16) & 0xff] & 0xff) << 16
             | (SBOX[(a >>> 8) & 0xff] & 0xff) << 8
             | (SBOX[a & 0xff] & 0xff);
    }

    /**
     * 线性变换L1 - 加密/解密用
     */
    private static int L1(int b) {
        return b ^ rotateLeft(b, 2) ^ rotateLeft(b, 10) ^ rotateLeft(b, 18) ^ rotateLeft(b, 24);
    }

    /**
     * 线性变换L2 - 密钥扩展用
     */
    private static int L2(int b) {
        return b ^ rotateLeft(b, 13) ^ rotateLeft(b, 23);
    }

    /**
     * 循环左移
     */
    private static int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /**
     * PKCS#7填充
     */
    private static byte[] pkcs7Pad(byte[] data) {
        int paddingLen = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        byte[] padded = new byte[data.length + paddingLen];
        System.arraycopy(data, 0, padded, 0, data.length);

        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLen;
        }

        return padded;
    }

    /**
     * 去除PKCS#7填充
     */
    private static byte[] pkcs7Unpad(byte[] data) {
        if (data.length == 0) {
            return data;
        }

        int paddingLen = data[data.length - 1] & 0xff;

        // 验证填充
        if (paddingLen < 1 || paddingLen > BLOCK_SIZE) {
            throw new IllegalArgumentException("Invalid padding");
        }

        for (int i = 1; i <= paddingLen; i++) {
            if ((data[data.length - i] & 0xff) != paddingLen) {
                throw new IllegalArgumentException("Invalid padding");
            }
        }

        byte[] unpadded = new byte[data.length - paddingLen];
        System.arraycopy(data, 0, unpadded, 0, unpadded.length);

        return unpadded;
    }

    /**
     * 字节数组转int（大端序）
     */
    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24)
             | ((bytes[offset + 1] & 0xff) << 16)
             | ((bytes[offset + 2] & 0xff) << 8)
             | (bytes[offset + 3] & 0xff);
    }

    /**
     * int转字节数组（大端序）
     */
    private static void intToBytes(int n, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((n >>> 24) & 0xff);
        bytes[offset + 1] = (byte) ((n >>> 16) & 0xff);
        bytes[offset + 2] = (byte) ((n >>> 8) & 0xff);
        bytes[offset + 3] = (byte) (n & 0xff);
    }
}
