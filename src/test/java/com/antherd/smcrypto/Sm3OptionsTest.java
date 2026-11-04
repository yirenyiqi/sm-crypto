package com.antherd.smcrypto;

import com.antherd.smcrypto.sm3.Sm3;
import com.antherd.smcrypto.sm3.Sm3Options;
import org.junit.Test;

import javax.script.ScriptException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Sm3OptionsTest {

    @Test
    public void testSm3WithoutOptions() throws ScriptException {
        String msg = "abc";
        String hashData = Sm3.sm3(msg);
        System.out.println("SM3(abc): " + hashData);
        assertNotNull(hashData);
        assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0", hashData);
    }

    @Test
    public void testSm3WithOptions() throws ScriptException {
        String msg = "abc";

        // 当前options支持key字段，为未来扩展预留
        Sm3Options options = new Sm3Options();
        options.setKey("testkey");

        String hashData = Sm3.sm3(msg, options);
        System.out.println("SM3(abc) with options: " + hashData);
        assertNotNull(hashData);

        // 当前实现key参数暂不影响结果，保持向后兼容
        assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0", hashData);
    }

    @Test
    public void testSm3WithEmptyOptions() throws ScriptException {
        String msg = "abc";
        Sm3Options options = new Sm3Options();

        String hashData = Sm3.sm3(msg, options);
        System.out.println("SM3(abc) with empty options: " + hashData);
        assertNotNull(hashData);
        assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0", hashData);
    }
}
