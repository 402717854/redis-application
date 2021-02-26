package com.redis.application.lock;

import com.redis.application.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisLock {
    private static volatile boolean threadState=Boolean.TRUE;

    public void lock(String key, String clientId, long expire)  {
        boolean lock = RedisUtils.lock(key, clientId, expire);
        if(lock){
            threadState=Boolean.TRUE;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (threadState) {
                        Long expire1 = RedisUtils.getExpire(key, TimeUnit.MILLISECONDS);
                        if (expire1 <=0) {
                            log.info("异步暂停线程");
                            threadState=Boolean.FALSE;
                        }
                        log.info("lock存在剩余时间:{}",expire1);
                        if (expire1> 1000 && expire1< 2000) {
                            log.info("异步线程续航lock");
                            RedisUtils.expire(key, expire);
                        }
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            try {
                log.info("业务处理-----");
                Thread.sleep(expire+5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                RedisUtils.safeUnLock(key,clientId);
                threadState=Boolean.FALSE;
                log.info("释放锁");
            }
        }
    }
}
