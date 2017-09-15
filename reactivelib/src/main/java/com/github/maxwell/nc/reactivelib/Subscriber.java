package com.github.maxwell.nc.reactivelib;

import com.github.maxwell.nc.reactivelib.subscription.Subscription;

/**
 * 订阅者<br/>
 * 执行回调的顺序可能的顺序如下：<br/>
 * onSubscribe(request方法前)->onNext->onComplete<br/>
 * onSubscribe(request方法前)->onNext->onError<br/>
 *
 * @param <T> 订阅的数据类型
 */
public interface Subscriber<T> {

    /**
     * 订阅操作回调
     *
     * @param s 订阅信息，可以请求订阅数据或者取消订阅数据
     */
    void onSubscribe(Subscription s);

    /**
     * 下一个数据处理回调
     *
     * @param t 数据元素
     */
    void onNext(T t);

    /**
     * 完成操作回调
     */
    void onComplete();

    /**
     * 遇到异常处理回调
     *
     * @param throwable 异常
     */
    void onError(Throwable throwable);

}
