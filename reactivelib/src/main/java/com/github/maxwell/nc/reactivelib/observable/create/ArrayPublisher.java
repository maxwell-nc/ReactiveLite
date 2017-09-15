package com.github.maxwell.nc.reactivelib.observable.create;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.FlowStream;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;


/**
 * 数组类型的生产者<br/>
 * 可以通过{@link Publisher#just(Object[])}方法创建<br/>
 */
public class ArrayPublisher<T> extends Publisher<T> {

    private final T[] array;

    public ArrayPublisher(final T[] array) {
        this.array = array;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        subscriber.onSubscribe(new ArraySubscription<>(subscriber, array));
    }

    private static final class ArraySubscription<T> extends FlowSubscription implements FlowStream<T> {

        private final Subscriber<T> actual;
        private final T[] array;

        /**
         * 数组最大下标为整型
         */
        private int index = 0;

        private ArraySubscription(Subscriber<T> subscriber, T[] array) {
            this.actual = subscriber;
            this.array = array;
        }

        @Override
        public T next() {
            T t = index == array.length ? null : array[index];
            index++;
            return t;
        }

        @Override
        public void request(long count) {
            try {
                while (!cancelled && index != count && index != array.length) {
                    actual.onNext(next());
                }
            } catch (Exception e) {
                cancel();
                actual.onError(e);
                return;
            }

            if (!cancelled) {
                actual.onComplete();
            }
        }

    }
}
