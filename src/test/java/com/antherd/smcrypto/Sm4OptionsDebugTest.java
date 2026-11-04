package com.antherd.smcrypto;

import com.antherd.smcrypto.sm4.Sm4;
import com.antherd.smcrypto.sm4.Sm4Options;
import org.junit.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class Sm4OptionsDebugTest {

    @Test
    public void testOptionsMap() {
        Sm4Options options = new Sm4Options();
        options.setMode("cbc");
        options.setIv("fedcba98765432100123456789abcdef");

        // 直接调用getOptionsMap查看内容
        Map<String, Object> optionsMap = new HashMap<>();
        String mode = options.getMode();
        if (mode != null && !"".equals(mode.trim())) {
            optionsMap.put("mode", mode);
        }
        String iv = options.getIv();
        if (iv != null && !"".equals(iv.trim())) {
            optionsMap.put("iv", iv);
        }

        System.out.println("Options Map: " + optionsMap);
        System.out.println("mode value: " + optionsMap.get("mode"));
        System.out.println("iv value: " + optionsMap.get("iv"));
        System.out.println("mode class: " + optionsMap.get("mode").getClass());
    }

    @Test
    public void testEcbVsCbc() throws ScriptException {
        String msg = "hello world!";
        String key = "0123456789abcdeffedcba9876543210";

        // ECB模式（默认）
        String ecbResult = Sm4.encrypt(msg, key);
        System.out.println("ECB result: " + ecbResult);

        // CBC模式
        Sm4Options cbcOptions = new Sm4Options();
        cbcOptions.setMode("cbc");
        cbcOptions.setIv("fedcba98765432100123456789abcdef");
        String cbcResult = Sm4.encrypt(msg, key, cbcOptions);
        System.out.println("CBC result: " + cbcResult);

        System.out.println("ECB == CBC? " + ecbResult.equals(cbcResult));
    }
}
