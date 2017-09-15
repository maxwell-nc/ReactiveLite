package com.github.maxwell.nc.reactivelib.callback;

/**
 * 数据流接口
 */
public interface FlowStream<T> {

    /**
     * 返回下一个数据
     */
    T next();

}
