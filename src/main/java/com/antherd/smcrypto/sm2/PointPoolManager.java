package com.antherd.smcrypto.sm2;

import javax.script.ScriptException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SM2椭圆曲线点池管理器
 * 预生成并管理椭圆曲线点，显著提升签名性能
 * 
 * @author geyiwei
 */
public class PointPoolManager {
    
    private static final int DEFAULT_POOL_SIZE = 100;
    private static final Queue<Point> DEFAULT_POOL = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    
    /**
     * 延迟初始化点池
     */
    private static void ensureInitialized() {
        if (!INITIALIZED.get()) {
            synchronized (PointPoolManager.class) {
                if (INITIALIZED.get()) {
                    return;
                }
                try {
                    for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
                        DEFAULT_POOL.add(Sm2.getPoint());
                    }
                    INITIALIZED.set(true);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize point pool", e);
                }
            }
        }
    }
    
    /**
     * 获取默认点池
     * 首次调用会自动初始化100个点
     * 
     * @return 预生成的椭圆曲线点池
     */
    public static Queue<Point> getDefaultPool() {
        ensureInitialized();
        return DEFAULT_POOL;
    }
    
    /**
     * 创建新的点池
     * 
     * @param size 点池大小
     * @return 新的点池
     * @throws ScriptException JavaScript引擎异常
     */
    public static Queue<Point> createPool(int size) throws ScriptException {
        if (size <= 0) {
            throw new IllegalArgumentException("Pool size must be positive");
        }
        Queue<Point> pool = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < size; i++) {
            pool.add(Sm2.getPoint());
        }
        return pool;
    }
    
    /**
     * 补充点池
     * 当点池数量不足时，补充到指定大小
     * 
     * @param pool 要补充的点池
     * @param targetSize 目标大小
     * @throws ScriptException JavaScript引擎异常
     */
    public static void refillPool(Queue<Point> pool, int targetSize) throws ScriptException {
        if (pool == null) {
            throw new IllegalArgumentException("Pool cannot be null");
        }
        int currentSize = pool.size();
        if (currentSize < targetSize) {
            for (int i = currentSize; i < targetSize; i++) {
                pool.add(Sm2.getPoint());
            }
        }
    }
    
    /**
     * 获取点池当前大小
     * 
     * @return 点池中的点数量
     */
    public static int getDefaultPoolSize() {
        return DEFAULT_POOL.size();
    }
}
