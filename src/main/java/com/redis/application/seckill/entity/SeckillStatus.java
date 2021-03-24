package com.redis.application.seckill.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SeckillStatus {
    //秒杀用户名
    private Integer userId;
    //创建时间
    private Date createTime;
    //秒杀状态  1:排队中，2:秒杀等待支付,3:支付超时，4:秒杀失败,5:支付完成
    private Integer status;
    //秒杀的商品ID
    private Long goodsId;

    public SeckillStatus() {
    }

    public SeckillStatus(Integer userId, Date createTime, Integer status, Long goodsId) {
        this.userId = userId;
        this.createTime = createTime;
        this.status = status;
        this.goodsId = goodsId;
    }
}
