package com.github.maxwell.nc.reactivelib.observable.create;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;


/**
 * 空的生产者<br/>
 * 直接执行onComplete()回调<br/>
 * 可以通过{@link Publisher#empty()}创建<br/>
 */
public class EmptyPublisher<T> extends Publisher<T> {

    @Override
    protected void subscribeActual(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new FlowSubscription() {

            @Override
            public void request(long count) {
                subscriber.onComplete();
            }

        });
    }

}
