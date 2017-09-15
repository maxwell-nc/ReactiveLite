package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 子线程调度器<br/>
 * 这里的子线程通过线程池管理，非并发线程调度器<br/>
 */
public class NewThreadSchedulers extends Schedulers {

    private final ScheduledExecutorService executor;

    public NewThreadSchedulers() {
        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void schedule(Runnable runnable) {
        executor.execute(runnable);
    }

}
