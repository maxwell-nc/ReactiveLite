package com.github.maxwell.nc.reactivelib.thread;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.scheduler.Scheduler;

/**
 * 订阅操作执行线程调度的生产者<br>
 * 指定特定调度器来控制生产者被订阅时的操作执行所在的线程，包括subscribe操作和onSubscribe回调<br>
 * 可以通过{@link Publisher#subscribeOn(Scheduler)}操作符来转换
 */
public class PublisherSubscribeOn<T> extends Publisher<T> {

    private final Publisher<T> source;
    private final Scheduler scheduler;

    public PublisherSubscribeOn(Publisher<T> source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(final Subscriber<T> subscriber) {
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                source.subscribe(subscriber);
            }

        });
    }

}
