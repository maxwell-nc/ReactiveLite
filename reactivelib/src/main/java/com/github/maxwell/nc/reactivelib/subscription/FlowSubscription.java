package com.github.maxwell.nc.reactivelib.subscription;

/**
 * 带取消的订阅信息<br>
 */
public abstract class FlowSubscription implements Subscription {

    /**
     * 取消标记
     */
    protected volatile boolean cancelled = false;

    /**
     * 返回是否已经取消订阅
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 取消订阅
     */
    @Override
    public void cancel() {
        cancelled = true;
    }

}
