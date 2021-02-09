package com.redis.application.delay.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 消息结构
 */
@Getter
@Setter
public class Job implements Serializable {

    private static final long serialVersionUID = -6042928182238814074L;

    /**
     * Job的唯一标识。用来检索和删除指定的Job信息
     */
    private String jobId;


    /**
     * Job类型。可以理解成具体的业务名称
     */
    private String topic;

    /**
     * Job需要延迟的时间。单位：秒。（服务端会将其转换为绝对时间）
     */
    private Long delay;

    /**
     * Job的内容，供消费者做具体的业务处理，以json格式存储
     */
    private String body;

    /**
     * 失败重试次数
     */
    private int retry = 0;

    /**
     * 通知URL
     */
    private String url;
}
