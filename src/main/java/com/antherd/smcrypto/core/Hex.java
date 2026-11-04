package com.antherd.smcrypto.core;

/**
 * 16进制编解码工具类
 *
 * @author geyiwei
 */
public class Hex {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 字节数组转16进制字符串
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        char[] chars = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xff;
            chars[i * 2] = HEX_CHARS[b >>> 4];
            chars[i * 2 + 1] = HEX_CHARS[b & 0x0f];
        }

        return new String(chars);
    }

    /**
     * 16进制字符串转字节数组
     */
    public static byte[] decode(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }

        // 补齐奇数长度
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * 左补零到指定长度
     */
    public static String leftPad(String input, int length) {
        if (input == null) {
            input = "";
        }

        if (input.length() >= length) {
            return input;
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = input.length(); i < length; i++) {
            sb.append('0');
        }
        sb.append(input);

        return sb.toString();
    }
}
