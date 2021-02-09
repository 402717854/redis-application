package com.redis.application.delay.service;


import com.redis.application.delay.entity.Job;
import com.redis.application.delay.entity.JobDie;

/**
 * 提供给外部服务的操作接口
 */
public interface RedisDelayQueueService {

    /**
     * 添加job元信息
     *
     * @param job 元信息
     */
    void addJob(Job job);


    /**
     * 删除job信息
     *
     * @param jobDie
     */
    void deleteJob(JobDie jobDie);
}
