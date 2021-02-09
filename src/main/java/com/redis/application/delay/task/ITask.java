package com.redis.application.delay.task;

/**
 * 任务接口
 *
 */
public interface ITask<T> {
    /**
     * 执行任务
     */
    void doTask();
}
