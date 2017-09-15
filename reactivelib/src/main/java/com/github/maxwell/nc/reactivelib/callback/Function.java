package com.github.maxwell.nc.reactivelib.callback;

/**
 * 功能接口
 */
public interface Function<T, R> {

    /**
     * 把元素t转换成R类型的元素返回
     *
     * @return R类型的元素
     */
    R apply(T t) throws Exception;

}
