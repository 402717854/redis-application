package com.redis.application.delay.scheduled;

import com.redis.application.constant.GlobalExceptionEnum;
import com.redis.application.delay.constant.RedisQueueKey;
import com.redis.application.exception.OperationException;
import com.redis.application.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CarryJobScheduled {

    /**
     * 启动定时开启搬运JOB信息
     */
    @Scheduled(cron = "*/1 * * * * *")
    public void carryJobToQueue() {
        log.info("启动搬运线程carryJobToQueue --->");
        try {
            boolean lockFlag = RedisUtils.lock(RedisQueueKey.CARRY_THREAD_LOCK, "1", RedisQueueKey.LOCK_WAIT_TIME);
            if (!lockFlag) {
                throw new OperationException(GlobalExceptionEnum.ACQUIRE_LOCK_FAIL);
            }
            long now = System.currentTimeMillis();
            //获取0--当前时间的zSet集合
            Set<Object> rangeByScore = RedisUtils.rangeByScore(RedisQueueKey.RD_ZSET_BUCKET_PRE, 0, now);
            if(CollectionUtils.isEmpty(rangeByScore)){
                return;
            }
            List<Object> jobList = rangeByScore.stream().map(String::valueOf).collect(Collectors.toList());
            //放入准备消费队列中
            RedisUtils.rPushAll(RedisQueueKey.RD_LIST_TOPIC_PRE,jobList);
            //zSet集合删除准备消费的消息
            RedisUtils.zRemove(RedisQueueKey.RD_ZSET_BUCKET_PRE,rangeByScore.toArray());
        } catch (Exception e) {
            log.error("carryJobToQueue error", e);
        } finally {
            RedisUtils.safeUnLock(RedisQueueKey.CARRY_THREAD_LOCK, "1");
        }
    }
}
