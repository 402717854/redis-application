package com.redis.application.delay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.redis.application.constant.GlobalExceptionEnum;
import com.redis.application.delay.constant.RedisQueueKey;
import com.redis.application.delay.entity.Job;
import com.redis.application.delay.entity.JobDie;
import com.redis.application.delay.service.RedisDelayQueueService;
import com.redis.application.exception.OperationException;
import com.redis.application.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RedisDelayQueueServiceImpl implements RedisDelayQueueService {



    @Override
    public void addJob(Job job) {
        try{
            boolean lockFlag = RedisUtils.lock(RedisQueueKey.ADD_JOB_LOCK+job.getJobId(), "1", RedisQueueKey.LOCK_WAIT_TIME);
            if (!lockFlag) {
                throw new OperationException(GlobalExceptionEnum.ACQUIRE_LOCK_FAIL);
            }
            String topicId = RedisQueueKey.getTopicId(job.getTopic(), job.getJobId());

            Map<String,Object> jobPoolMap = JSONObject.parseObject(JSONObject.toJSONString(job), Map.class);
            // 1. 将job添加到 JobPool中
            RedisUtils.hmSet(RedisQueueKey.JOB_POOL_KEY,topicId, jobPoolMap);
            // 2. 将job添加到 DelayBucket中
            RedisUtils.zAdd(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId,job.getDelay());
        }catch (Exception e){
            log.error("addJob error", e);
        }finally {
            RedisUtils.safeUnLock(RedisQueueKey.ADD_JOB_LOCK+job.getJobId(),"1");
        }
    }

    @Override
    public void deleteJob(JobDie jobDie) {
        try{
            boolean lockFlag = RedisUtils.lock(RedisQueueKey.ADD_JOB_LOCK+jobDie.getJobId(), "1", RedisQueueKey.LOCK_WAIT_TIME);
            if (!lockFlag) {
                throw new OperationException(GlobalExceptionEnum.ACQUIRE_LOCK_FAIL);
            }
            String topicId = RedisQueueKey.getTopicId(jobDie.getTopic(), jobDie.getJobId());

            // 1. 从JobPool中删除job
            RedisUtils.hmRemove(RedisQueueKey.JOB_POOL_KEY,topicId);
            // 2. 从DelayBucket中删除
            RedisUtils.zRemove(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId);
        }catch (Exception e){
            log.error("addJob error", e);
        }finally {
            RedisUtils.safeUnLock(RedisQueueKey.ADD_JOB_LOCK+jobDie.getJobId(),"1");
        }
    }
}
