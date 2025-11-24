package com.easymusic.entity.enums;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cyt
 * * @date 2025/11/24 20:56:03
 */

// 单例模式，线程池
public enum ExecutorServiceSingletonEnum {
    INSTANCE;

    private final ExecutorService executorService;

    ExecutorServiceSingletonEnum() {
        executorService = Executors.newFixedThreadPool(10);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
