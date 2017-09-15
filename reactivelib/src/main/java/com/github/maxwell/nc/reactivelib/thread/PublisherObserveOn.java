package com.github.maxwell.nc.reactivelib.thread;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.scheduler.Schedulers;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 观察者执行的线程控制的生产者<br/>
 * 指定特定调度器来控制观察者回调中onNext、onComplete和onError回调执行的线程<br/>
 * 通过操作符{@link Publisher#observeOn(Schedulers)}转换生产者
 */
public class PublisherObserveOn<T> extends Publisher<T> {

    private final Publisher<T> source;
    private final Schedulers schedulers;

    public PublisherObserveOn(Publisher<T> source, Schedulers schedulers) {
        this.source = source;
        this.schedulers = schedulers;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        source.subscribe(new ObserveOnSubscriber<>(subscriber, schedulers));
    }

    private static final class ObserveOnSubscriber<T> implements Subscriber<T> {

        private final Subscriber<T> actual;
        private final Schedulers schedulers;
        private FlowSubscription flowSubscription;

        public ObserveOnSubscriber(Subscriber<T> subscriber, Schedulers schedulers) {
            actual = subscriber;
            this.schedulers = schedulers;
        }

        @Override
        public void onSubscribe(final Subscription s) {
            if (s instanceof FlowSubscription) {
                flowSubscription = (FlowSubscription) s;
            }
            //onSubscribe不在调度方法中
            actual.onSubscribe(s);
        }

        @Override
        public void onNext(final T t) {
            if (flowSubscription.isCancelled()) {
                return;
            }
            schedulers.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!flowSubscription.isCancelled()) {
                            actual.onNext(t);
                        }
                    } catch (Exception e) {
                        flowSubscription.cancel();
                        onError(e);
                    }
                }
            });
        }

        @Override
        public void onComplete() {
            schedulers.schedule(new Runnable() {
                @Override
                public void run() {
                    if (!flowSubscription.isCancelled()) {
                        actual.onComplete();
                    }
                }
            });
        }

        @Override
        public void onError(final Throwable throwable) {
            schedulers.schedule(new Runnable() {
                @Override
                public void run() {
                    actual.onError(throwable);
                }
            });
        }

    }
}
