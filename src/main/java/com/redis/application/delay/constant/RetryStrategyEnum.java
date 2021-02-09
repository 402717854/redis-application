package com.redis.application.delay.constant;

import lombok.Getter;

import java.util.Arrays;

/**
 * 配置消费失败的重试策略
 *
 * 通知频率为 5/10/60/300/1800/，单位：秒
 *
 * @author 睁眼看世界
 * @date 2020年1月17日
 */
@Getter
public enum RetryStrategyEnum {
    RETRY_ONE(1,5),
    RETRY_TWO(2,10),
    RETRY_THREE(3,60),
    RETRY_FOUR(4,300),
    RETRY_FIVE(5,1800);

    /**
     * 重试次数
     */
    private int retry;

    /**
     * 延时
     */
    private int delay;


    RetryStrategyEnum(Integer retry, Integer delay) {
        this.retry = retry;
        this.delay = delay;
    }

    /**
     * 根据重试次数
     * @param retry 次数
     * @return 延时
     */
    public static int getDelayTime(int retry){
        return Arrays.stream(values()).filter(e->e.getRetry() == retry).map(RetryStrategyEnum::getDelay).findFirst().orElse(RETRY_FIVE.getDelay());
    }

    public static void main(String[] args) {
        int delayTime = RetryStrategyEnum.getDelayTime(6);
        System.out.println(delayTime);
    }
}
