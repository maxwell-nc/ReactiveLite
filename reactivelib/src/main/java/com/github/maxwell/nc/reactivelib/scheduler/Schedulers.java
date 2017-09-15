package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executor;

/**
 * 调度器管理器
 */
public abstract class Schedulers {

    /**
     * 创建子线程调度器
     */
    public static Schedulers newThread() {
        return new NewThreadSchedulers();
    }

    /**
     * 创建UI线程调度器（同主线程）
     */
    public static Schedulers uiThread() {
        return mainThread();
    }

    /**
     * 创建主线程调度器
     */
    public static Schedulers mainThread() {
        return new MainThreadSchedulers();
    }

    /**
     * 创建并行调度器<br/>
     * 并行上限为CPU处理器数量
     */
    public static Schedulers parallel() {
        return parallel(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 创建指定数量的并行调度器
     *
     * @param poolSize 线程池大小，若<=0则为无限制，若为1则为单线程
     */
    public static Schedulers parallel(int poolSize) {
        return new ParallelSchedulers(poolSize);
    }

    /**
     * 通过Executor创建调度器
     *
     * @param executor 指定的Executor
     */
    public static Schedulers form(Executor executor) {
        return new ParallelSchedulers(executor);
    }

    /**
     * 实际调度的方法
     *
     * @param runnable 需要执行的操作
     */
    public abstract void schedule(Runnable runnable);

}
