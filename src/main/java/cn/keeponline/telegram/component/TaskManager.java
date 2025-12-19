package cn.keeponline.telegram.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class TaskManager {

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    private final ConcurrentHashMap<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    public void start(String uid, Runnable task) {
        // 防止重复启动
        stop(uid);

        ScheduledFuture<?> future =
            scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(3));

        futures.put(uid, future);
    }

    public void stop(String uid) {
        ScheduledFuture<?> f = futures.remove(uid);
        if (f != null) {
            f.cancel(false);
        }
    }

    public void stopAll() {
        futures.keySet().forEach(this::stop);
    }
}