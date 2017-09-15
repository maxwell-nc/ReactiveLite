package com.github.maxwell.nc.reactivelib.observable.handle;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 遇到错误重试的生产者<br/>
 * 若遇到异常，则会重新调用订阅操作，从头开始请求数据（包括重新走onSubscribe回调）<br/>
 * 特别注意：此生产者在多线程并发情况下可能无法执行重试操作<br/>
 * 通过指定{@link #retryTimes}为重试次数，若重试次数用尽则不会再重试，保留最后一次的状态<br/>
 * 此生产者可以通过{@link Publisher#retry(int)}操作符来实现转换
 */
public class RetryPublisher<T> extends Publisher<T> {

    private final Publisher<T> source;
    private final int retryTimes;

    public RetryPublisher(Publisher<T> source, int times) {
        this.source = source;
        retryTimes = times;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        source.subscribe(new RetrySubscriber<>(source, subscriber, retryTimes));
    }

    private static final class RetrySubscriber<T> implements Subscriber<T> {

        private final Publisher<T> source;
        private final Subscriber<T> actual;
        private int retryTimes;
        private FlowSubscription subscription;

        public RetrySubscriber(Publisher<T> source, Subscriber<T> subscriber, int times) {
            this.source = source;
            actual = subscriber;
            retryTimes = times;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (s instanceof FlowSubscription) {
                subscription = (FlowSubscription) s;
            }
            actual.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            try {
                if (!subscription.isCancelled()) {
                    actual.onNext(t);
                }
            } catch (Exception e) {
                subscription.cancel();
                onError(e);
            }
        }

        @Override
        public void onComplete() {
            if (!subscription.isCancelled()) {
                actual.onComplete();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (retryTimes != 0) {
                retryTimes--;
                source.subscribe(this);//重试
            } else {
                actual.onError(throwable);
            }
        }
    }

}
