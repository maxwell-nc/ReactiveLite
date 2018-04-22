package com.github.maxwell.nc.reactivelib;

import com.github.maxwell.nc.reactivelib.callback.Action;
import com.github.maxwell.nc.reactivelib.callback.Consumer;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * Lambda表达式订阅者<br>
 */
public class LambdaSubscriber<T> implements Subscriber<T> {

    private final Consumer<T> onNext;
    private final Action onComplete;
    private final Consumer<? super Throwable> onError;
    private FlowSubscription subscription;

    public LambdaSubscriber(Consumer<T> onNext, Action onComplete, Consumer<? super Throwable> onError) {
        this.onNext = onNext;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (s instanceof FlowSubscription) {
            subscription = (FlowSubscription) s;
        }
        //请求所有可用数据
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        if (onNext != null) {
            if (subscription != null && subscription.isCancelled()) {
                return;
            }
            try {
                onNext.accept(t);
            } catch (Exception e) {
                if (subscription != null) {
                    subscription.cancel();
                }
                onError(e);
            }
        }
    }

    @Override
    public void onComplete() {
        if (onComplete != null) {
            if (subscription != null && subscription.isCancelled()) {
                return;
            }
            try {
                onComplete.run();
            } catch (Exception e) {
                uncaught(e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (onError != null) {
            try {
                onError.accept(throwable);
            } catch (Exception ex) {
                uncaught(throwable);
            }
        }
    }

    /**
     * 抛出未处理异常（订阅回调逻辑本身代码的问题）
     */
    private static void uncaught(Throwable throwable) {
        Thread currentThread = Thread.currentThread();
        Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, throwable);
    }
}
