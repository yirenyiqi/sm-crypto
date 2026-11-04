package com.antherd.smcrypto.sm3;

import com.antherd.smcrypto.NashornProvider;
import com.antherd.smcrypto.Provider;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author geyiwei
 */
public class Sm3 {
    static {
        NashornProvider.printNonNashorn();
    }
    private static Invocable invocable = null;

    static {
        try {
            invocable = (Invocable) Provider.getJavaScriptEngine(Provider.SM3_CLASSPATH_RESOURCE_PATH);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    /**
     * 杂凑
     *
     * @param msg 明文
     * @param sm3Options SM3配置
     * @return 杂凑
     * @throws ScriptException Scripting通用异常
     */
    public static String sm3(String msg, Sm3Options sm3Options) throws ScriptException {
        if (msg == null || msg.trim().isEmpty()) {
            return "";
        }
        String hashData = null;
        try {
            hashData = (String) invocable.invokeFunction("constructSm3", msg, getOptionsMap(sm3Options));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return hashData;
    }

    /**
     * 杂凑
     *
     * @param msg 明文
     * @return 杂凑
     * @throws ScriptException Scripting通用异常
     */
    public static String sm3(String msg) throws ScriptException {
        return sm3(msg, null);
    }

    /**
     * 若sm3配置中为默认值，js调用时不添加此参数
     *
     * @param sm3Options SM3配置
     * @return SM3配置Map
     */
    private static Map<String, Object> getOptionsMap(Sm3Options sm3Options) {
        Map<String, Object> options = new HashMap<>();
        if (sm3Options == null) {
            return options;
        }
        String key = sm3Options.getKey();
        if (key != null && !"".equals(key.trim())) {
            options.put("key", key);
        }
        return options;
    }
}
