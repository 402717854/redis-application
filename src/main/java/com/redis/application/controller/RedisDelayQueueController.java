package com.redis.application.controller;

import com.alibaba.fastjson.JSON;
import com.redis.application.delay.constant.RedisQueueKey;
import com.redis.application.delay.entity.Job;
import com.redis.application.delay.service.RedisDelayQueueService;
import com.redis.application.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RedisDelayQueueController {

    @Autowired
    private RedisDelayQueueService redisDelayQueueService;

    @GetMapping("/redisDelayQueue")
    public void redisDelayQueue(){
        for (int i = 0; i < 10; i++) {
            Job job = new Job();
            job.setRetry(0);
            job.setJobId(i+"");
            job.setBody("延迟队列消息体"+i);
            job.setTopic("topic:"+i);
            job.setDelay(System.currentTimeMillis());
            redisDelayQueueService.addJob(job);
        }
    }
}
