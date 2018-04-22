package com.github.maxwell.nc.reactivelib.scheduler;

/**
 * 调度器接口
 */
public interface Scheduler {

    /**
     * 实际调度的方法
     *
     * @param runnable 需要执行的操作
     */
    void schedule(Runnable runnable);

}
