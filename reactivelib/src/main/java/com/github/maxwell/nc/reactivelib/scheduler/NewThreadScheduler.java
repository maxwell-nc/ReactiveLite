package com.github.maxwell.nc.reactivelib.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 子线程调度器<br/>
 * 每次创建新线程的调度器<br/>
 */
public class NewThreadScheduler implements Scheduler {

    @Override
    public void schedule(Runnable runnable) {
        new Thread(runnable).start();
    }

}
