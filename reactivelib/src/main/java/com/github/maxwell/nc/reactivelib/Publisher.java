package com.github.maxwell.nc.reactivelib;

import com.github.maxwell.nc.reactivelib.callback.Action;
import com.github.maxwell.nc.reactivelib.callback.Consumer;
import com.github.maxwell.nc.reactivelib.callback.Function;
import com.github.maxwell.nc.reactivelib.callback.Predicate;
import com.github.maxwell.nc.reactivelib.observable.create.ArrayPublisher;
import com.github.maxwell.nc.reactivelib.observable.create.EmptyPublisher;
import com.github.maxwell.nc.reactivelib.observable.create.ErrorPublisher;
import com.github.maxwell.nc.reactivelib.observable.create.IterablePublisher;
import com.github.maxwell.nc.reactivelib.observable.create.TimerPublisher;
import com.github.maxwell.nc.reactivelib.observable.handle.ErrorReturnPublisher;
import com.github.maxwell.nc.reactivelib.observable.handle.RetryPublisher;
import com.github.maxwell.nc.reactivelib.observable.transform.BufferPublisher;
import com.github.maxwell.nc.reactivelib.observable.transform.MapPublisher;
import com.github.maxwell.nc.reactivelib.observable.transform.SelectPublisher;
import com.github.maxwell.nc.reactivelib.scheduler.Scheduler;
import com.github.maxwell.nc.reactivelib.scheduler.Schedulers;
import com.github.maxwell.nc.reactivelib.thread.PublisherObserveOn;
import com.github.maxwell.nc.reactivelib.thread.PublisherSubscribeOn;

import java.util.List;

/**
 * 生产者
 */
public abstract class Publisher<T> {

    /**
     * 创建：空的生产者，直接执行onComplete()回调
     *
     * @see EmptyPublisher
     */
    public static <T> Publisher<T> empty() {
        return new EmptyPublisher<>();
    }

    /**
     * 创建：错误的生产者，直接执行onError()回调
     *
     * @see ErrorPublisher
     */
    public static <T> Publisher<T> error(Throwable throwable) {
        return new ErrorPublisher<>(throwable);
    }

    /**
     * 创建：数组类型的数据生产者
     *
     * @param t 可变数据列，数据为空则返回空的生产者
     * @see ArrayPublisher
     */
    @SafeVarargs
    public static <T> Publisher<T> just(final T... t) {
        if (t.length == 0) {
            return empty();
        }
        return new ArrayPublisher<>(t);
    }

    /**
     * 创建：可迭代数据类型的数据生产者
     *
     * @param iterable 可迭代数据，数据为空则返回空的生产者
     * @see IterablePublisher
     */
    public static <T> Publisher<T> from(Iterable<T> iterable) {
        if (iterable == null) {
            return empty();
        }
        return new IterablePublisher<>(iterable);
    }

    /**
     * 创建：定时器数据生产者，定时器线程在运行所在的线程
     *
     * @param interval 间隔毫秒（必须大于0）
     * @see TimerPublisher
     */
    public static Publisher<Long> timer(long interval) {
        return new TimerPublisher(interval, null);
    }

    /**
     * 创建：定时器数据生产者
     *
     * @param interval   间隔毫秒（必须大于0）
     * @param scheduler 定时器运行线程调度器
     * @see TimerPublisher
     */
    public static Publisher<Long> timer(long interval, Scheduler scheduler) {
        if (interval <= 0) {
            throw new IndexOutOfBoundsException("interval is out of bounds!");
        }
        return new TimerPublisher(interval, scheduler);
    }

    /**
     * 转换：变换数据的生产者
     *
     * @param function 非空，转换操作回调
     * @param <V>      转换后的数据类型
     * @see MapPublisher
     */
    public <V> Publisher<V> map(Function<T, V> function) {
        if (function == null) {
            throw new NullPointerException("map function is null!");
        }
        return new MapPublisher<>(this, function);
    }

    /**
     * 转换：缓存数据的生产者，缓存一定数量的数据然后合并为List发送
     *
     * @param bufferSize 缓存数量（必须大于0）
     * @see BufferPublisher
     */
    public Publisher<List<T>> buffer(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IndexOutOfBoundsException("bufferSize is out of bounds!");
        }
        return new BufferPublisher<>(this, bufferSize);
    }

    /**
     * 转换：筛选数据的生产者
     *
     * @param predicate 筛选逻辑
     * @see SelectPublisher
     */
    public Publisher<T> select(Predicate<T> predicate) {
        if (predicate == null) {
            return this;
        }
        return new SelectPublisher<>(this, predicate);
    }

    /**
     * 处理：遇到错误(异常）重试的生产者
     *
     * @param times 重试次数（必须大于0）
     * @see RetryPublisher
     */
    public Publisher<T> retry(int times) {
        if (times <= 0) {
            throw new IndexOutOfBoundsException("retry times is out of bounds!");
        }
        return new RetryPublisher<>(this, times);
    }

    /**
     * 处理：遇到错误（异常）返回特定数据的生产者
     *
     * @param function 处理错误及返回数据的回调
     * @see ErrorReturnPublisher
     */
    public Publisher<T> errorReturn(Function<Throwable, T> function) {
        if (function == null) {
            return this;
        }
        return new ErrorReturnPublisher<>(this, function);
    }

    /**
     * 调度：订阅操作执行线程调度的生产者
     *
     * @param scheduler 调度器，可以从{@link Schedulers}创建
     * @see PublisherSubscribeOn
     */
    public Publisher<T> subscribeOn(Scheduler scheduler) {
        if (scheduler == null) {
            return this;
        }
        return new PublisherSubscribeOn<>(this, scheduler);
    }

    /**
     * 调度：观察者执行的线程控制的生产者
     *
     * @param scheduler 调度器，可以从{@link Schedulers}创建
     * @see PublisherObserveOn
     */
    public Publisher<T> observeOn(Scheduler scheduler) {
        if (scheduler == null) {
            return this;
        }
        return new PublisherObserveOn<>(this, scheduler);
    }


    /**
     * 订阅生产者
     *
     * @param onNext 下一个数据操作回调
     */
    public final void subscribe(Consumer<T> onNext) {
        subscribe(onNext, null, null);
    }


    /**
     * 订阅生产者
     *
     * @param onNext  下一个数据操作回调
     * @param onError 错误操作回调
     */
    public final void subscribe(Consumer<T> onNext, Consumer<? super Throwable> onError) {
        subscribe(onNext, onError, null);
    }

    /**
     * 订阅生产者
     *
     * @param onNext     下一个数据操作回调
     * @param onError    错误操作回调
     * @param onComplete 完成操作回调
     */
    public final void subscribe(Consumer<T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        subscribe(new LambdaSubscriber<>(onNext, onComplete, onError));
    }

    /**
     * 订阅生产者
     *
     * @param subscriber 订阅者
     */
    public final void subscribe(Subscriber<T> subscriber) {
        subscribeActual(subscriber);
    }

    /**
     * 实际订阅操作方法
     *
     * @param subscriber 订阅者
     */
    protected abstract void subscribeActual(Subscriber<T> subscriber);

}
