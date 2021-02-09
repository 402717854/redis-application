package com.redis.application.delay.constant;

/**
 * Redis队列KEY值常量
 */
public class RedisQueueKey {

    /**
     * 是一个Hash_Table结构；里面存储了所有的延迟队列的信息;KV结构；
     * K=prefix + projectName    field= topic+jobId    V=CONENT;  V由客户端传入的数据,消费的时候回传；
     */
    public static final String JOB_POOL_KEY = "ZING:DELAY_QUEUE:JOB_POOL";


    /**
     * 延迟队列的有序集合; 存放K=ID 和需要的执行时间戳;
     * 根据时间戳排序;
     */
    public static final String RD_ZSET_BUCKET_PRE = "ZING:DELAY_QUEUE:BUCKET";


    /**
     * list结构; 每个Topic一个list；list存放的都是当前需要被消费的Job;
     */
    public static final String RD_LIST_TOPIC_PRE = "ZING:DELAY_QUEUE:QUEUE";

    /**
     * 返回成功
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * 搬运线程分布式锁
     */
    public static final String CARRY_THREAD_LOCK = "ZING:DELAY_QUEUE:CARRY_THREAD_LOCK";

    /**
     * 添加JOB分布式锁
     */
    public static final String ADD_JOB_LOCK = "ZING:DELAY_QUEUE:ADD_JOB_LOCK:";

    /**
     * 删除JOB分布式锁
     */
    public static final String DELETE_JOB_LOCK = "ZING:DELAY_QUEUE:DELETE_JOB_LOCK:";

    /**
     * 分布式锁
     */
    public static final String CONSUMER_TOPIC_LOCK = "ZING:DELAY_QUEUE:CONSUMER_TOPIC_LOCK";

    /**
     * 锁等待时间
     */
    public static final long LOCK_WAIT_TIME = 3000;

    /**
     * 锁释放时间
     */
    public static final long LOCK_RELEASE_TIME = 61000;

    /**
     * 拼接TOPICID
     */
    public static String getTopicId(String topic, String id) {
        return topic.concat(":").concat(id);
    }

}
