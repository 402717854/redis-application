package com.redis.application.delay.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.util.function.Supplier;

/**
 * 记录运行时间
 *
 */
@Slf4j
public class SpeedTimeLogSuit {

    private SpeedTimeLogSuit() {
    }

    /**
     * 执行并打印执行时间
     *
     * @param supplier 执行方法
     * @param title    标题
     * @param <T>      返回值类型
     * @return 返回值
     */
    public static <T> T wrap(Supplier<T> supplier, String title) {
        log.info("SPEED_TIME_LOG:[{}], start to run", title);
        StopWatch watch = new StopWatch();
        watch.start();
        T result = supplier.get();
        watch.stop();
        log.info("SPEED_TIME_LOG:[{}] run completed in {} ms", title, watch.getTotalTimeMillis());
        return result;
    }
}
