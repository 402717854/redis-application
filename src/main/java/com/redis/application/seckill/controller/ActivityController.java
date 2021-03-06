package com.redis.application.seckill.controller;

import com.redis.application.seckill.service.ActivityService;
import com.redis.application.seckill.service.SecKillService;
import com.redis.application.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private SecKillService secKillService;

    @GetMapping("secKillThread")
    public void secKillOrderThread(){
        long startTime = new Date().getTime();
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    secKillService.activityOrder(123l,456, finalI);
                }
            });
            thread.start();
        }
        long endTime = new Date().getTime();
        System.out.println("运行时间"+(endTime-startTime));
    }
    @GetMapping("setRedisGoodsInfo")
    public void setRedisGoodsInfo(@RequestParam Long goodsId,@RequestParam int activityId){
        Date date = new Date();
        long time = date.getTime();
        RedisUtils.hmSet("activity:" + activityId, "startTime",time);
        RedisUtils.hmSet("activity:" + activityId, "endTime",time+24*60*60*1000);
        RedisUtils.set("secKillGoods_stock:activityId_" + activityId + ":goodsId_" + goodsId,20l);
    }
    @GetMapping("secKill")
    public void secKillOrder(){
        activityService.activityOrder(123l,456, 123456);
    }
    @GetMapping("testDecrement")
    public void testDecrement(){
        RedisUtils.set("testDecrement",20l);
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    RedisUtils.safeDecrement("testDecrement");
                }
            });
            thread.start();
        }
    }
}
