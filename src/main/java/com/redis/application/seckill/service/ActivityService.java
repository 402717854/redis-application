package com.redis.application.seckill.service;

import org.springframework.stereotype.Component;


public interface ActivityService {

    public void activityOrder(Long goodsId,Integer activityId,Integer userId);

}
