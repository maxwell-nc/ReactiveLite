package com.github.maxwell.nc.reactivelib.observable.transform;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.Function;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 变换数据的生产者<br>
 * 通过指定{@link #function}来设置变换数据的回调，把T类型原数据转换成R类型的新数据<br>
 * 可以通过{@link Publisher#map(Function)}来转换
 */
public class MapPublisher<T, R> extends Publisher<R> {

    private final Publisher<T> source;
    private final Function<T, R> function;

    public MapPublisher(Publisher<T> source, Function<T, R> function) {
        this.source = source;
        this.function = function;
    }

    @Override
    protected void subscribeActual(Subscriber<R> subscriber) {
        source.subscribe(new MapSubscriber<>(subscriber, function));
    }

    private static final class MapSubscriber<T, R> implements Subscriber<T> {
        private final Subscriber<R> actual;
        private final Function<T, R> function;
        private FlowSubscription subscription;

        MapSubscriber(Subscriber<R> subscriber, Function<T, R> function) {
            this.actual = subscriber;
            this.function = function;
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
                actual.onNext(function.apply(t));
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
