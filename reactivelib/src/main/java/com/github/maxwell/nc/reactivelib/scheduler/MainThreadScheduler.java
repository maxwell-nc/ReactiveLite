package com.github.maxwell.nc.reactivelib.scheduler;

import android.os.Handler;
import android.os.Looper;

/**
 * 主线程调度器
 */
public class MainThreadScheduler implements Scheduler {

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void schedule(Runnable runnable) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            mainHandler.post(runnable);
        } else {//在主线程直接执行
            runnable.run();
        }
    }
}
