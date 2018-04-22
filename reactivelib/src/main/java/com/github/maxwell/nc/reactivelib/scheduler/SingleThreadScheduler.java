package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 单一子线程复用调度器<br>
 */
public class SingleThreadScheduler implements Scheduler {

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void schedule(Runnable runnable) {
        executor.execute(runnable);
    }

}
