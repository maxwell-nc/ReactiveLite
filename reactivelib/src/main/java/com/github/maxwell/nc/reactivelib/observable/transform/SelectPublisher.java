package com.github.maxwell.nc.reactivelib.observable.transform;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.Predicate;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 筛选数据的生产者<br>
 * 通过设定的{@link #predicate}来筛选数据，若{@link Predicate#test(Object)}方法返回true则调用onNext事件，否则抛弃元素<br>
 * 可以通过{@link Publisher#select(Predicate)}操作符来转换
 */
public class SelectPublisher<T> extends Publisher<T> {

    private final Publisher<T> source;
    private final Predicate<T> predicate;

    public SelectPublisher(Publisher<T> source, Predicate<T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        source.subscribe(new SelectSubscriber<>(subscriber, predicate));
    }

    private static final class SelectSubscriber<T> implements Subscriber<T> {

        private final Subscriber<T> actual;
        private final Predicate<T> predicate;
        private FlowSubscription subscription;

        SelectSubscriber(Subscriber<T> subscriber, Predicate<T> predicate) {
            actual = subscriber;
            this.predicate = predicate;
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
                if (subscription != null && subscription.isCancelled()) {
                    return;
                }
                if (predicate.test(t)) {
                    actual.onNext(t);
                }
            } catch (Exception e) {
                if (subscription != null) {
                    subscription.cancel();
                }
                onError(e);
            }
        }

        @Override
        public void onComplete() {
            if (subscription != null && subscription.isCancelled()) {
                return;
            }
            actual.onComplete();
        }

        @Override
        public void onError(Throwable throwable) {
            actual.onError(throwable);
        }
    }

}
