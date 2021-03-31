package com.redis.application.config;

import com.redis.application.seckill.mq.RocketMQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RocketMQConfig {
    @Bean
    public RocketMQProducer getRocketMQProducer(){
        String mqNameServer = "127.0.0.1:9876";
        String mqTopics = "SECKILL-TOPICS-TEST";

        String producerMqGroupName = "PRODUCER-MQ-GROUP";
        RocketMQProducer mqProducer = new RocketMQProducer(mqNameServer, producerMqGroupName, mqTopics);

        mqProducer.init();
        return mqProducer;
    }
}
