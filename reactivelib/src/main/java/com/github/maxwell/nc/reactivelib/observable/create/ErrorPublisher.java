package com.github.maxwell.nc.reactivelib.observable.create;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;

/**
 * 错误的生产者<br/>
 * 直接执行onError()回调<br/>
 * 可以通过{@link Publisher#error(Throwable)}来创建
 */
public class ErrorPublisher<T> extends Publisher<T> {

    private final Throwable throwable;

    public ErrorPublisher(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    protected void subscribeActual(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new FlowSubscription() {

            @Override
            public void request(long count) {
                subscriber.onError(throwable);
            }

        });
    }
}
