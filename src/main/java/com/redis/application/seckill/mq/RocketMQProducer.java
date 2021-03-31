package com.redis.application.seckill.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import java.util.UUID;

public class RocketMQProducer {

    private DefaultMQProducer sender;

    protected String nameServer;

    protected String groupName;

    protected String topics;

    public void init() {
        sender = new DefaultMQProducer(groupName);
        sender.setNamesrvAddr(nameServer);
        sender.setInstanceName(UUID.randomUUID().toString());
        sender.setSendMsgTimeout(10000);
        //同步模式下发送消息的次数
        sender.setRetryTimesWhenSendFailed(0);
        try {
            sender.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public RocketMQProducer(String nameServer, String groupName, String topics) {
        this.nameServer = nameServer;
        this.groupName = groupName;
        this.topics = topics;
    }

    public void send(Message message) throws Exception{

        message.setTopic(topics);

        SendResult result = sender.send(message);
        SendStatus status = result.getSendStatus();
        if(status.equals(SendStatus.SEND_OK)){
            System.out.println("messageId=" + result.getMsgId() + ", status=" + status);
        }else{
            throw new RuntimeException("发送消息失败");
        }
    }
}
