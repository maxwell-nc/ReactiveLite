package com.github.maxwell.nc.reactivelib.subscription;

/**
 * 订阅信息接口
 */
public interface Subscription {

    /**
     * 请求数据<br>
     * 注意调用此方法应该放在onSubscribe方法中的最后一行，否则onSubscribe中的操作会在onNext等回调之后<br>
     *
     * @param count 请求数量，指定{@link Long#MAX_VALUE}则为无限请求(直到生产者生产完毕)
     */
    void request(long count);

    /**
     * 取消请阅
     */
    void cancel();

}
