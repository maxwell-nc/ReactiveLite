package com.github.maxwell.nc.reactivelib.observable.create;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.FlowStream;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;

import java.util.Iterator;


/**
 * 可迭代的数据生产者<br/>
 * 可以通过{@link Publisher#from(Iterable)}创建<br/>
 */
public class IterablePublisher<T> extends Publisher<T> {

    private final Iterable<T> iterable;

    public IterablePublisher(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        subscriber.onSubscribe(new IterableSubscription<>(subscriber, iterable));
    }

    private static final class IterableSubscription<T> extends FlowSubscription implements FlowStream<T> {

        private final Subscriber<T> actual;
        private final Iterator<T> iterator;

        private long index = 0;

        private IterableSubscription(Subscriber<T> subscriber, Iterable<T> t) {
            this.actual = subscriber;
            iterator = t.iterator();

        }

        @Override
        public T next() {
            T t = iterator.next();
            index++;
            return t;
        }

        @Override
        public void request(long count) {
            try {
                while (!cancelled && index != count && iterator.hasNext()) {
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
