package com.redis.application.controller;

import com.redis.application.lock.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisLockController {

    @Autowired
    private RedisLock redisLock;

    @GetMapping("lock")
    public void lock(){
        redisLock.lock("redislock","123456",10000);
    }
}
