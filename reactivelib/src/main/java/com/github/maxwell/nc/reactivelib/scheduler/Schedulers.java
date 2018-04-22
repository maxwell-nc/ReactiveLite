package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executor;

/**
 * 调度器管理器
 */
public final class Schedulers {

    private static SingleThreadScheduler singleThreadScheduler;
    private static NewThreadScheduler newThreadScheduler;
    private static MainThreadScheduler mainThreadScheduler;
    private static ParallelScheduler parallelScheduler;

    /**
     * 单一子线程调度器
     */
    public static synchronized Scheduler single() {
        if (singleThreadScheduler == null) {
            singleThreadScheduler = new SingleThreadScheduler();
        }
        return singleThreadScheduler;
    }

    /**
     * 创建子线程调度器
     */
    public static Scheduler newThread() {
        if (newThreadScheduler == null) {
            newThreadScheduler = new NewThreadScheduler();
        }
        //虽然是单一的调度器，内部实现创建线程
        return newThreadScheduler;
    }

    /**
     * 创建UI线程调度器（同主线程）
     */
    public static Scheduler uiThread() {
        return mainThread();
    }

    /**
     * 创建主线程调度器
     */
    public static Scheduler mainThread() {
        if (mainThreadScheduler == null) {
            mainThreadScheduler = new MainThreadScheduler();
        }
        return mainThreadScheduler;
    }

    /**
     * 创建并行调度器<br>
     * 并行上限为CPU处理器数量
     */
    public static Scheduler parallel() {
        if (parallelScheduler == null) {
            parallelScheduler = new ParallelScheduler(Runtime.getRuntime().availableProcessors());
        }
        return parallelScheduler;
    }

    /**
     * 通过Executor创建调度器
     *
     * @param executor 指定的Executor
     */
    public static Scheduler form(final Executor executor) {
        return new Scheduler() {
            @Override
            public void schedule(Runnable runnable) {
                executor.execute(runnable);
            }
        };
    }

}
