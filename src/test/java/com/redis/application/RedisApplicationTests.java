package com.redis.application;

import com.redis.application.delay.service.RedisDelayQueueService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class RedisApplicationTests {

    @Autowired
    private RedisDelayQueueService redisDelayQueueService;

    @Test
    void contextLoads() {
    }

}
