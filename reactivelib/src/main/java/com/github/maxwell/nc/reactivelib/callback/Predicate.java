package com.github.maxwell.nc.reactivelib.callback;

/**
 * 功能接口
 */
public interface Predicate<T> {

    /**
     * 断言元素t是否符合某个逻辑
     *
     * @return 是否符合规则
     */
    boolean test(T t) throws Exception;

}
