package com.redis.application.seckill.service.impl;

import com.redis.application.seckill.entity.SeckillOrder;
import com.redis.application.seckill.entity.SeckillStatus;
import com.redis.application.util.RedisUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {
    @Async
    public void createOrder(Integer activityId){
        SeckillStatus seckillStatus = (SeckillStatus) RedisUtils.leftPop("secKillGoods_Order_Queue_Up:activityId_" + activityId);
        if(seckillStatus!=null&&seckillStatus.getStatus()==1){
            Integer userId = seckillStatus.getUserId();
            Long increment = RedisUtils.decrement("secKillGoods_stock:activityId_" + activityId + ":goodsId_" + seckillStatus.getGoodsId());
            if(increment<0){
                //秒杀失败
                seckillStatus.setStatus(4);
                RedisUtils.hmSet("secKillGoods_isBuying:activityId_"+activityId,seckillStatus.getUserId(),seckillStatus);
                System.out.println(userId+"商品库存剩余"+increment+"已售罄!");
                return;
            }
            if(increment==0){
                //同步数据库
                System.out.println(userId+"同步数据库,已售罄");
            }
            try {
                //创建订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setUserId(userId+"");
                seckillOrder.setSeckillId(seckillStatus.getGoodsId());
                seckillOrder.setCreateTime(new Date());
                //用户抢购标志变更
                seckillStatus.setStatus(2);
                RedisUtils.hmSet("secKillGoods_isBuying:activityId_" + activityId, seckillStatus.getUserId(),seckillStatus);
                System.out.println(userId+"MQ保证发送执行抢购订单成功消息");
//                if(userId==18||userId==17||userId==16){
//                   int i=1/0;
//                }
                //删除下单成功的用户
                seckillStatus.setStatus(3);
                RedisUtils.hmSet("secKillGoods_isBuying:activityId_" + activityId, seckillStatus.getUserId(),seckillStatus);
                System.out.println("下单成功");
            }catch (Exception e){
                seckillStatus.setStatus(4);
                RedisUtils.hmSet("secKillGoods_isBuying:activityId_" + activityId, seckillStatus.getUserId(),seckillStatus);
                throw new RuntimeException(userId+"下单失败，通过补偿机制对下单失败的用户进行人工处理");
            }
        }
    }
}
