package com.redis.application.delay.task;

/**
 * 任务接口
 */
public interface IFutureTask<T> {

    /**
     * 执行任务
     *
     * @return 任务返回值
     */
    T doTask();
}
