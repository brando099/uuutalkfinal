package cn.keeponline.telegram.task;

import java.util.concurrent.*;

public class ThreadPoolUtil {

    // 核心线程数（CPU核心数 × 2）
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int KEEP_ALIVE_TIME = 60;

    // 队列大小
    private static final int QUEUE_CAPACITY = 200;

    // 自定义线程池
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private int count = 1;
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "custom-thread-" + count++);
                }
            },
            new ThreadPoolExecutor.AbortPolicy() // 队列满时抛异常
    );

    // 提交任务（无返回值）
    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }

    // 提交任务（有返回值）
    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    // 关闭线程池
    public static void shutdown() {
        EXECUTOR.shutdown();
    }

    // 获取线程池状态
    public static ThreadPoolExecutor getExecutor() {
        return EXECUTOR;
    }
}