package com.redis.application.seckill.service.impl;

import com.redis.application.seckill.entity.SeckillStatus;
import com.redis.application.seckill.service.ActivityService;
import com.redis.application.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

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
        SeckillStatus checkSeckillStatus = (SeckillStatus) RedisUtils.hmGet("secKillGoods_isBuying:activityId_" + activityId, userId);
        if(checkSeckillStatus!=null){
            //3、判断用户在此活动中是否存在未支付的订单
            if(checkSeckillStatus.getStatus()==2){
                System.out.println(userId+"存在未支付的订单");
                return;
            }
            //4、判断用户在此活动是否正在抢购商品
            if(checkSeckillStatus.getStatus()==1){
                System.out.println(userId+"您已经在抢购商品");
                return;
            }
        }
        //5、判断是否有库存
        Integer stock = (Integer) RedisUtils.get("secKillGoods_stock:activityId_" + activityId + ":goodsId_" + goodsId);
        if(stock<=0){
            System.out.println(userId+"abc活动商品已售罄"+stock);
            return;
        }
        Long aLong = RedisUtils.listSize("secKillGoods_Order_Queue_Up:activityId_" + activityId);
        if(aLong>stock){
            System.out.println(userId+"查询排队抢购失败,队列长度:"+aLong+",库存:"+stock);
            return;
        }
        //6、放入redis抢购队列中
        //创建秒杀队列数据，秒杀排队
        SeckillStatus seckillStatus = new SeckillStatus(userId, new Date(), 1, goodsId);
        Long listSize = RedisUtils.rPush("secKillGoods_Order_Queue_Up:activityId_" + activityId, seckillStatus);
        if(listSize>stock){
            //需要对队列进行清空补偿机制
            System.out.println(userId+"排队抢购失败,队列长度:"+listSize+",库存:"+stock);
            return;
        }
        //7、设置用户正在抢购商品
        RedisUtils.hmSet("secKillGoods_isBuying:activityId_" + activityId, userId,seckillStatus);
        //8、异步下单
        multiThreadingCreateOrder.createOrder(activityId);
    }
}
