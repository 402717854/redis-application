package com.redis.application.controller;

import com.alibaba.fastjson.JSON;
import com.redis.application.delay.constant.RedisQueueKey;
import com.redis.application.delay.entity.Job;
import com.redis.application.delay.service.RedisDelayQueueService;
import com.redis.application.sensitive.SensitiveWordRedisFilter;
import com.redis.application.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class RedisDelayQueueController {

    @Autowired
    private RedisDelayQueueService redisDelayQueueService;

    @Autowired
    private SensitiveWordRedisFilter sensitiveWordRedisFilter;

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
    @GetMapping("/checkSensitiveWord")
    public void checkSensitiveWord(String txt){
        boolean contains = sensitiveWordRedisFilter.contains(txt);
        System.out.println(contains);
    }
    @GetMapping("/initSensitiveWord")
    public void initSensitiveWord(){
        sensitiveWordRedisFilter.initSensitiveWordBySources();
    }
    @GetMapping("/addSensitiveWord")
    public void addSensitiveWord(String sensitiveWord){
        sensitiveWordRedisFilter.addSensitiveWord(sensitiveWord);
    }
    @PostMapping("/addSensitiveWordSet")
    public void addSensitiveWordSet(@RequestBody SensitiveWordReq sensitiveWordReq){
        if(sensitiveWordReq==null){
            System.out.println("敏感词不能为空");
        }
        List<String> sensitiveWordList = sensitiveWordReq.getSensitiveWordList();
        if(CollectionUtils.isEmpty(sensitiveWordList)){
            System.out.println("敏感词不能为空");
        }
        Set<String> sensitiveWordSet = new HashSet<>(sensitiveWordList);
        sensitiveWordRedisFilter.addSensitiveWordSet(sensitiveWordSet);
    }
}
