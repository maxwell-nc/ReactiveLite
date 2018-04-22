package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 多线程并发调度器<br>
 * 特别注意：使用此调度器后数据可能不是按照原始顺序接收<br>
 * 可以通过指定线程池大小，若指定值小于等于0则创建无限制线程池，如果等于1则创建单线程线程池<br>
 */
public class ParallelScheduler implements Scheduler {

    private final Executor executor;

    public ParallelScheduler(Executor executor) {
        this.executor = executor;
    }

    public ParallelScheduler(int poolSize) {
        if (poolSize <= 0) {
            executor = Executors.newCachedThreadPool();
        } else if (poolSize == 1) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = Executors.newFixedThreadPool(poolSize);
        }
    }

    @Override
    public void schedule(Runnable runnable) {
        executor.execute(runnable);
    }

}
