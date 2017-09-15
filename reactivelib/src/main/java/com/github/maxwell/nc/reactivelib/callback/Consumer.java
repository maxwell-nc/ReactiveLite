package com.github.maxwell.nc.reactivelib.callback;

/**
 * 功能接口
 */
public interface Consumer<T> {

    /**
     * 消耗元素t的操作逻辑
     */
    void accept(T t);

}
