package com.redis.application.delay.task;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Supplier;


/**
 * 任务执行管理
 *
 */
@Slf4j
public class TaskManager {

    private TaskManager() {
    }

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 异步执行任务
     *
     * @param task  任务
     * @param title 标题
     */
    public static void doTask(final ITask task, String title) {
        Supplier<Runnable> supplier = new Supplier<Runnable>() {
            @Override
            public Runnable get() {
                try {
                    task.doTask();
                } catch (Exception e) {
                    log.error("TaskManager doTask execute error.", e);
                }
                return null;
            }
        };
        executorService.execute(SpeedTimeLogSuit.wrap(supplier,title));
    }

    /**
     * 返回值阻塞的task
     *
     * @param task  任务
     * @param title 标题
     */
    public static <T> FutureTask<T> doFutureTask(final IFutureTask<T> task, String title) {
        log.info(title);
        FutureTask futureTask = new FutureTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return task.doTask();
            }
        });
        executorService.execute(futureTask);
        return futureTask;
    }
    /**
     * 返回值不阻塞的task
     *
     * @param task  任务
     * @param title 标题
     */
    public static <T> CompletableFuture<T> doCompletableFutureTask(final IFutureTask<T> task, String title) {

        Supplier<T> supplier = new Supplier<T>() {
            @Override
            public T get(){
                return task.doTask();
            }
        };
        CompletableFuture<T> supplyAsync = CompletableFuture.supplyAsync(supplier);

        return supplyAsync;
    }
}
