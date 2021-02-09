package com.redis.application.delay.scheduled;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.redis.application.delay.constant.RedisQueueKey;
import com.redis.application.delay.constant.RetryStrategyEnum;
import com.redis.application.delay.entity.Job;
import com.redis.application.delay.task.IFutureTask;
import com.redis.application.delay.task.ITask;
import com.redis.application.delay.task.TaskManager;
import com.redis.application.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
@Component
public class ReadyQueueContext implements CommandLineRunner {
    /**
     * TOPIC消费线程
     */
    @PostConstruct
    public void startTopicConsumer() {
        System.out.println("看看项目启动加载顺序");
    }
    @Override
    public void run(String... args) throws Exception {
        TaskManager.doTask(this::runTopicThreads, "开启TOPIC消费线程");
    }

    /**
     * 开启TOPIC消费线程
     * 将所有可能出现的异常全部catch住，确保While(true)能够不中断
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private void runTopicThreads() {
        while (true) {
            try {
                // 分布式锁时间比Blpop阻塞时间多1S，避免出现释放锁的时候，锁已经超时释放，unlock报错
                boolean lockFlag = RedisUtils.lock(RedisQueueKey.CONSUMER_TOPIC_LOCK, "1", RedisQueueKey.LOCK_RELEASE_TIME);
                if (!lockFlag) {
                    continue;
                }
            } catch (Exception e) {
                log.error("runTopicThreads getLock error", e);
            }
            try {
                // 1. 获取ReadyQueue中待消费的数据  如果为空则在timeout内进行阻塞超时返回null
                Object rightPop = RedisUtils.leftPop(RedisQueueKey.RD_LIST_TOPIC_PRE);
                if(rightPop==null){
                    continue;
                }
                String topicId=(String)rightPop;
                log.info("对消息主题为:"+topicId+"进行消费");
                // 2. 获取job元信息内容
                Map<Object, Object> jobPoolMap = (Map<Object, Object>) RedisUtils.hmGet(RedisQueueKey.JOB_POOL_KEY,topicId);
                if(CollectionUtils.isEmpty(jobPoolMap)){
                    continue;
                }
                Job job = JSON.parseObject(JSON.toJSONString(jobPoolMap), Job.class);

                // 3. 消费
                //异步执行调用taskResult.get()返回阻塞
//                FutureTask<Boolean> taskResult = TaskManager.doFutureTask(new IFutureTask<Boolean>() {
//
//                    @Override
//                    public Boolean doTask() {
//                        log.info("=============异步消费消息返回阻塞==================");
//                        return true;
//                    }
//
//                },job.getTopic() + "-->消费JobId-->" + job.getJobId());

                //异步执行返回不阻塞
                CompletableFuture<Boolean> completableFutureTask = TaskManager.doCompletableFutureTask(new IFutureTask<Boolean>() {
                    @Override
                    public Boolean doTask() {
                        log.info("=============异步消费消息返回不阻塞==================");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                }, job.getTopic() + "-->消费JobId-->" + job.getJobId());
                //如果执行成功
                completableFutureTask.thenAccept(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        log.info("===================异步执行结果通知:{}========================",aBoolean);
                       if(aBoolean){
                           // 3.1 消费成功，删除JobPool和DelayBucket的job信息
                           removeTopic(topicId);
                       }else{
                           log.info("对主题为:"+topicId+"的消息消费失败进行重试");
                           int retrySum = job.getRetry() + 1;
                           // 3.2 消费失败，则根据策略重新加入Bucket
                           // 如果重试次数大于5，则将jobPool中的数据删除，持久化到DB记录日志
                           if (retrySum > RetryStrategyEnum.RETRY_FIVE.getRetry()) {
                               RedisUtils.hmRemove(RedisQueueKey.JOB_POOL_KEY,topicId);
                               RedisUtils.zRemove(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId);
                               return;
                           }
                           job.setRetry(retrySum);
                           retryStrategy(topicId,job);
                       }
                    }
                });
                //执行出现异常
                completableFutureTask.exceptionally(new Function<Throwable, Boolean>() {
                    @Override
                    public Boolean apply(Throwable throwable) {
                        String message = throwable.getMessage();
                        log.error(message);
                        log.info("对主题为:{}的消息消费失败进行第{}次重试",topicId,job.getRetry());
                        int retrySum = job.getRetry() + 1;
                        // 3.2 消费失败，则根据策略重新加入Bucket
                        // 如果重试次数大于5，则将jobPool中的数据删除，持久化到DB记录日志
                        if (retrySum > RetryStrategyEnum.RETRY_FIVE.getRetry()) {
                            RedisUtils.hmRemove(RedisQueueKey.JOB_POOL_KEY,topicId);
                            RedisUtils.zRemove(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId);
                            return true;
                        }
                        job.setRetry(retrySum);
                        retryStrategy(topicId,job);
                        return false;
                    }
                });
//                if (taskResult.get()) {
//                    // 3.1 消费成功，删除JobPool和DelayBucket的job信息
//                    removeTopic(topicId);
//                } else {
//                    log.info("对主题为:"+topicId+"的消息消费失败进行重试");
//                    int retrySum = job.getRetry() + 1;
//                    // 3.2 消费失败，则根据策略重新加入Bucket
//                    // 如果重试次数大于5，则将jobPool中的数据删除，持久化到DB记录日志
//                    if (retrySum > RetryStrategyEnum.RETRY_FIVE.getRetry()) {
//                        RedisUtils.hmRemove(RedisQueueKey.JOB_POOL_KEY,topicId);
//                        RedisUtils.zRemove(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId);
//                        continue;
//                    }
//                    job.setRetry(retrySum);
//                    retryStrategy(topicId,job,jobPoolMap);
//                }
            } catch (Exception e) {
                log.error("runTopicThreads error", e);
            } finally {
                try {
                    RedisUtils.safeUnLock(RedisQueueKey.CONSUMER_TOPIC_LOCK, "1");
                } catch (Exception e) {
                    log.error("runTopicThreads unlock error", e);
                }
            }
        }
    }
   private void removeTopic(String topicId){
       // 3.1 消费成功，删除JobPool和DelayBucket的job信息
       RedisUtils.hmRemove(RedisQueueKey.JOB_POOL_KEY,topicId);
       log.info("对主题为:"+topicId+"的已消费消息在队列"+RedisQueueKey.JOB_POOL_KEY+"中进行删除");
   }

   private void retryStrategy(String topicId,Job job){
       Map<String,Object> jobPoolMap = JSONObject.parseObject(JSONObject.toJSONString(job), Map.class);
       long nextTime = job.getDelay() + RetryStrategyEnum.getDelayTime(job.getRetry()) * 1000;
       log.info("next retryTime is [{}]", DateUtil.date(nextTime));
       RedisUtils.zAdd(RedisQueueKey.RD_ZSET_BUCKET_PRE,topicId,nextTime);
       // 3.3 更新元信息失败次数
       RedisUtils.hmSet(RedisQueueKey.JOB_POOL_KEY,topicId, jobPoolMap);
   }
}
