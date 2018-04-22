package com.github.maxwell.nc.reactivelib.observable.transform;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存数据的生产者<br>
 * 每次调用onNext事件时，先缓存到List中，若缓存已满则输出结果，若调用onComplete事件还存在缓存没输出则一并输出<br>
 * 通过制定{@link #bufferSize}来指定每次缓存的数据数量<br>
 * 此方法使用ArrayList作为缓存列表，注意若数据量大，而{@link #bufferSize}设置值过小，则会创建大量的ArrayList<br>
 * 此生产者可以通过{@link Publisher#buffer(int)}转换
 */
public class BufferPublisher<T> extends Publisher<List<T>> {

    private final Publisher<T> source;
    private final int bufferSize;

    public BufferPublisher(Publisher<T> source, int size) {
        this.source = source;
        bufferSize = size;
    }

    @Override
    protected void subscribeActual(Subscriber<List<T>> subscriber) {
        source.subscribe(new BufferSubscriber<>(subscriber, bufferSize));
    }

    private static final class BufferSubscriber<T> implements Subscriber<T> {

        private final Subscriber<List<T>> actual;
        private final int bufferSize;
        private FlowSubscription subscription;

        private List<T> tempList;

        BufferSubscriber(Subscriber<List<T>> subscriber, int size) {
            actual = subscriber;
            bufferSize = size;
            tempList = new ArrayList<>(bufferSize);
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
                int size = tempList.size();
                if (size < bufferSize) {
                    tempList.add(t);
                } else if (size == bufferSize) {
                    actual.onNext(tempList);
                    tempList = null;
                    //用新的缓存
                    tempList = new ArrayList<>(bufferSize);
                    tempList.add(t);
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
            if (tempList.size() > 0) {
                actual.onNext(tempList);
            }
            actual.onComplete();
        }

        @Override
        public void onError(Throwable throwable) {
            actual.onError(throwable);
        }
    }

}
