package com.antherd.smcrypto.core;

import java.nio.charset.StandardCharsets;

/**
 * SM3密码杂凑算法 - 纯Java实现
 * 国密算法标准: GM/T 0004-2012
 *
 * @author geyiwei
 */
public class SM3Digest {
    private static final int DIGEST_LENGTH = 32; // 256位 = 32字节
    private static final int BLOCK_SIZE = 64;    // 512位 = 64字节

    // IV初始值
    private static final int[] IV = {
        0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600,
        0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e
    };

    private final int[] V = new int[8];
    private final int[] W = new int[68];
    private final int[] M = new int[64]; // W'

    public SM3Digest() {
        reset();
    }

    /**
     * 重置状态
     */
    public void reset() {
        System.arraycopy(IV, 0, V, 0, IV.length);
    }

    /**
     * 计算SM3哈希值
     *
     * @param data 输入数据
     * @return 32字节哈希值
     */
    public byte[] digest(byte[] data) {
        reset();
        return doFinal(data);
    }

    /**
     * 计算SM3哈希值并返回16进制字符串
     */
    public String digestHex(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] hash = digest(data);
        return Hex.encode(hash);
    }

    /**
     * 完成哈希计算
     */
    private byte[] doFinal(byte[] data) {
        // 填充消息
        byte[] padded = padding(data);

        // 迭代压缩
        int n = padded.length / BLOCK_SIZE;
        for (int i = 0; i < n; i++) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(padded, i * BLOCK_SIZE, block, 0, BLOCK_SIZE);
            compress(block);
        }

        // 输出哈希值
        byte[] result = new byte[DIGEST_LENGTH];
        for (int i = 0; i < 8; i++) {
            intToBytes(V[i], result, i * 4);
        }

        return result;
    }

    /**
     * 消息填充
     * k是满足 len + 1 + k = 448 mod 512 的最小非负整数
     */
    private byte[] padding(byte[] data) {
        int len = data.length;
        long bitLen = (long) len * 8;

        // 计算填充长度
        int k = (448 - ((len * 8 + 1) % 512) + 512) % 512;
        int paddingLen = (k + 1) / 8;
        int totalLen = len + paddingLen + 8;

        byte[] padded = new byte[totalLen];
        System.arraycopy(data, 0, padded, 0, len);

        // 添加1后跟k个0
        padded[len] = (byte) 0x80;

        // 添加64位长度
        for (int i = 0; i < 8; i++) {
            padded[totalLen - 8 + i] = (byte) ((bitLen >>> (56 - i * 8)) & 0xff);
        }

        return padded;
    }

    /**
     * 压缩函数
     */
    private void compress(byte[] block) {
        // 消息扩展
        // W0-W15: 直接从消息分组获取
        for (int i = 0; i < 16; i++) {
            W[i] = bytesToInt(block, i * 4);
        }

        // W16-W67
        for (int j = 16; j < 68; j++) {
            W[j] = P1(W[j - 16] ^ W[j - 9] ^ rotateLeft(W[j - 3], 15))
                   ^ rotateLeft(W[j - 13], 7) ^ W[j - 6];
        }

        // W'0-W'63
        for (int j = 0; j < 64; j++) {
            M[j] = W[j] ^ W[j + 4];
        }

        // 压缩
        int A = V[0], B = V[1], C = V[2], D = V[3];
        int E = V[4], F = V[5], G = V[6], H = V[7];

        for (int j = 0; j < 64; j++) {
            int SS1 = rotateLeft(rotateLeft(A, 12) + E + rotateLeft(T(j), j), 7);
            int SS2 = SS1 ^ rotateLeft(A, 12);
            int TT1 = FF(A, B, C, j) + D + SS2 + M[j];
            int TT2 = GG(E, F, G, j) + H + SS1 + W[j];

            D = C;
            C = rotateLeft(B, 9);
            B = A;
            A = TT1;
            H = G;
            G = rotateLeft(F, 19);
            F = E;
            E = P0(TT2);
        }

        V[0] ^= A;
        V[1] ^= B;
        V[2] ^= C;
        V[3] ^= D;
        V[4] ^= E;
        V[5] ^= F;
        V[6] ^= G;
        V[7] ^= H;
    }

    /**
     * 布尔函数FF
     */
    private int FF(int X, int Y, int Z, int j) {
        return (j >= 0 && j <= 15) ? (X ^ Y ^ Z) : ((X & Y) | (X & Z) | (Y & Z));
    }

    /**
     * 布尔函数GG
     */
    private int GG(int X, int Y, int Z, int j) {
        return (j >= 0 && j <= 15) ? (X ^ Y ^ Z) : ((X & Y) | (~X & Z));
    }

    /**
     * 置换函数P0
     */
    private int P0(int X) {
        return X ^ rotateLeft(X, 9) ^ rotateLeft(X, 17);
    }

    /**
     * 置换函数P1
     */
    private int P1(int X) {
        return X ^ rotateLeft(X, 15) ^ rotateLeft(X, 23);
    }

    /**
     * 常量T
     */
    private int T(int j) {
        return (j >= 0 && j <= 15) ? 0x79cc4519 : 0x7a879d8a;
    }

    /**
     * 循环左移
     */
    private int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /**
     * 字节数组转int
     */
    private int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24)
             | ((bytes[offset + 1] & 0xff) << 16)
             | ((bytes[offset + 2] & 0xff) << 8)
             | (bytes[offset + 3] & 0xff);
    }

    /**
     * int转字节数组
     */
    private void intToBytes(int n, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((n >>> 24) & 0xff);
        bytes[offset + 1] = (byte) ((n >>> 16) & 0xff);
        bytes[offset + 2] = (byte) ((n >>> 8) & 0xff);
        bytes[offset + 3] = (byte) (n & 0xff);
    }

    /**
     * HMAC-SM3实现
     */
    public static byte[] hmac(byte[] input, byte[] key) {
        int blockLen = 64;
        byte[] iPad = new byte[blockLen];
        byte[] oPad = new byte[blockLen];

        for (int i = 0; i < blockLen; i++) {
            iPad[i] = 0x36;
            oPad[i] = 0x5c;
        }

        // 密钥填充
        if (key.length > blockLen) {
            SM3Digest digest = new SM3Digest();
            key = digest.digest(key);
        }

        byte[] paddedKey = new byte[blockLen];
        System.arraycopy(key, 0, paddedKey, 0, key.length);

        // 计算iPadKey和oPadKey
        byte[] iPadKey = xor(paddedKey, iPad);
        byte[] oPadKey = xor(paddedKey, oPad);

        // hash(iPadKey || input)
        SM3Digest digest1 = new SM3Digest();
        byte[] temp = new byte[iPadKey.length + input.length];
        System.arraycopy(iPadKey, 0, temp, 0, iPadKey.length);
        System.arraycopy(input, 0, temp, iPadKey.length, input.length);
        byte[] hash1 = digest1.digest(temp);

        // hash(oPadKey || hash1)
        SM3Digest digest2 = new SM3Digest();
        temp = new byte[oPadKey.length + hash1.length];
        System.arraycopy(oPadKey, 0, temp, 0, oPadKey.length);
        System.arraycopy(hash1, 0, temp, oPadKey.length, hash1.length);

        return digest2.digest(temp);
    }

    /**
     * 字节数组异或
     */
    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
}
