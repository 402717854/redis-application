package com.redis.application.delay.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息结构
 *
 */
@Data
public class JobDie implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Job的唯一标识。用来检索和删除指定的Job信息
     */
    private String jobId;


    /**
     * Job类型。可以理解成具体的业务名称
     */
    private String topic;
}
