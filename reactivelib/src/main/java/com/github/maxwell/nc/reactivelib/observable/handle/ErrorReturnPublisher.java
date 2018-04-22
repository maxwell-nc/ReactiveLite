package com.github.maxwell.nc.reactivelib.observable.handle;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.Function;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 错误时候返回特定数据的生产者<br>
 * 通过指定特定数据，在遇到异常时先调用onNext回调发送指定数据，<b>再执行onComplete回调</b><br>
 * 若果指定的{@link #function}操作存在异常，则直接调用onError操作，不再发送特定数据<br>
 * 可以通过{@link Publisher#errorReturn(Function)}操作符转换出此生产者
 */
public class ErrorReturnPublisher<T> extends Publisher<T> {

    private final Publisher<T> source;
    private final Function<Throwable, T> function;

    public ErrorReturnPublisher(Publisher<T> source, Function<Throwable, T> function) {
        this.source = source;
        this.function = function;
    }

    @Override
    protected void subscribeActual(Subscriber<T> subscriber) {
        source.subscribe(new ErrorReturnSubscriber<>(subscriber, function));
    }

    private static final class ErrorReturnSubscriber<T> extends FlowSubscription implements Subscriber<T> {

        private final Subscriber<T> actual;
        private final Function<Throwable, T> function;
        private FlowSubscription subscription;

        /**
         * 是否运行取消
         */
        private boolean isAllowCancel = false;

        ErrorReturnSubscriber(Subscriber<T> subscriber, Function<Throwable, T> function) {
            this.actual = subscriber;
            this.function = function;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (s instanceof FlowSubscription) {
                subscription = (FlowSubscription) s;
            }
            //这里订阅的Subscription是此类
            actual.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            try {
                if (subscription != null && subscription.isCancelled()) {
                    return;
                }
                actual.onNext(t);
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
            try {
                actual.onNext(function.apply(throwable));
            } catch (Exception ex) {
                isAllowCancel = true;//执行完发送特定数据后取消其他操作
                cancel();
                actual.onError(ex);
                return;
            }
            actual.onComplete();
        }

        @Override
        public void request(long count) {
            subscription.request(count);
        }

        @Override
        public void cancel() {
            if (isAllowCancel) {
                subscription.cancel();
            }
        }

    }


}
