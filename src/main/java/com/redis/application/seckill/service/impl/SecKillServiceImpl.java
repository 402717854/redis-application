package com.redis.application.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.redis.application.seckill.entity.SeckillOrder;
import com.redis.application.seckill.entity.SeckillStatus;
import com.redis.application.seckill.mq.RocketMQProducer;
import com.redis.application.seckill.service.SecKillService;
import com.redis.application.util.RedisUtils;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private RocketMQProducer rocketMQProducer;
    @Override
    public void activityOrder(Long goodsId, Integer activityId, Integer userId) {
        //1、判断用户是否登录

        //2、判断活动是否开始
        long time = new Date().getTime();
        Long startTime = (Long) RedisUtils.hmGet("activity:" + activityId, "startTime");
        Long endTime = (Long) RedisUtils.hmGet("activity:" + activityId, "endTime");
        if(time<startTime){
            System.out.println("活动未开始");
            return;
        }
        if(time>endTime){
            System.out.println("活动已结束");
            return;
        }
        Long safeDecrement = (Long) RedisUtils.safeDecrement("secKillGoods_stock:activityId_" + activityId + ":goodsId_" + goodsId);
        if(safeDecrement<0){
            //秒杀失败
            System.out.println(userId+"商品库存剩余"+safeDecrement+"已售罄!");
            return;
        }
        System.out.println(userId+"剩余库存"+safeDecrement);
        if(safeDecrement==0){
            //同步数据库
            System.out.println(userId+"同步数据库,已售罄");
        }
        //创建订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(userId+"");
        seckillOrder.setSeckillId(goodsId);
        seckillOrder.setCreateTime(new Date());
        System.out.println(userId+"MQ保证发送执行抢购订单成功消息");
        Message message = new Message();
        message.setBody(JSON.toJSONString(seckillOrder).getBytes(StandardCharsets.UTF_8));
        try {
            rocketMQProducer.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            RedisUtils.increment("secKillGoods_stock:activityId_" + activityId + ":goodsId_" + goodsId);
        }
    }
}
