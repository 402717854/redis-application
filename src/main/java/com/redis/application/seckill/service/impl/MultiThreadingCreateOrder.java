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
        SeckillStatus seckillStatus = (SeckillStatus) RedisUtils.leftPop("seckillGoods_Order_Queue_Up" + activityId);
        if(seckillStatus!=null&&seckillStatus.getStatus()==1){
            Integer userId = seckillStatus.getUserId();
            Long increment = RedisUtils.decrement("seckillGoods_stock_" + activityId + ":goodsId-" + seckillStatus.getGoodsId());
            System.out.println("商品库存剩余"+increment);
            if(increment<0){
                //秒杀失败
                seckillStatus.setStatus(4);
                RedisUtils.hmSet("seckillGoods_isBuying_"+activityId,seckillStatus.getUserId(),seckillStatus);
                System.out.println(userId+"已售罄!");
                return;
            }
            if(increment==0){
                //同步数据库
                System.out.println(userId+"同步数据库,已售罄");
            }
            try {
                //创建订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setSeckillId(seckillStatus.getGoodsId());
                seckillOrder.setCreateTime(new Date());
                //用户抢购标志变更
                seckillStatus.setStatus(2);
                RedisUtils.hmSet("seckillGoods_isBuying_" + activityId, seckillStatus.getUserId(),seckillStatus);
                System.out.println(userId+"执行抢购订单");
                if(userId==18||userId==17||userId==16){
                   int i=1/0;
                }
            }catch (Exception e){
                seckillStatus.setStatus(4);
                RedisUtils.hmSet("seckillGoods_isBuying_" + activityId, seckillStatus.getUserId(),seckillStatus);
                RedisUtils.increment("seckillGoods_stock_" + activityId + ":goodsId-" + seckillStatus.getGoodsId());
                throw new RuntimeException(userId+"下单失败");
            }
        }
    }
}
