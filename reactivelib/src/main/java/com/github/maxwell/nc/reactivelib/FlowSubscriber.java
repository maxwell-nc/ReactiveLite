package com.github.maxwell.nc.reactivelib;

import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 方便取消的订阅者
 */
public abstract class FlowSubscriber<T> implements Subscriber<T> {

    private FlowSubscription flowSubscription;

    @Override
    public void onSubscribe(Subscription s) {
        if (s instanceof FlowSubscription) {
            flowSubscription = (FlowSubscription) s;
        }
        s.request(getRequestCount());
    }

    /**
     * 获取请求量
     */
    protected long getRequestCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    /**
     * 取消任务
     */
    public void cancelTask() {
        if (flowSubscription != null) {
            flowSubscription.cancel();
        }
    }

}
