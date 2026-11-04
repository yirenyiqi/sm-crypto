package com.antherd.smcrypto;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;

public class Sm4JsDebugTest {

    public static void main(String[] args) throws Exception {
        // 加载sm4.js
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        engine.eval(new java.io.BufferedReader(new java.io.InputStreamReader(
            Sm4JsDebugTest.class.getClassLoader().getResourceAsStream("sm4.js"))));

        Invocable invocable = (Invocable) engine;

        String msg = "hello world!";
        String key = "0123456789abcdeffedcba9876543210";

        // 测试1：不传options
        System.out.println("=== 测试1: 不传options ===");
        String result1 = (String) invocable.invokeFunction("encrypt", msg, key);
        System.out.println("Result: " + result1);

        // 测试2：传递空Map
        System.out.println("\n=== 测试2: 传递空Map ===");
        Map<String, Object> emptyMap = new HashMap<>();
        String result2 = (String) invocable.invokeFunction("encrypt", msg, key, emptyMap);
        System.out.println("Result: " + result2);

        // 测试3：传递包含mode的Map
        System.out.println("\n=== 测试3: 传递包含mode的Map ===");
        Map<String, Object> cbcMap = new HashMap<>();
        cbcMap.put("mode", "cbc");
        cbcMap.put("iv", "fedcba98765432100123456789abcdef");
        System.out.println("Sending map: " + cbcMap);
        String result3 = (String) invocable.invokeFunction("encrypt", msg, key, cbcMap);
        System.out.println("Result: " + result3);

        System.out.println("\nResult1 == Result2? " + result1.equals(result2));
        System.out.println("Result1 == Result3? " + result1.equals(result3));
    }
}
