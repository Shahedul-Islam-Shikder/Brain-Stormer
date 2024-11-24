package brain.brainstormer.utils;

import java.util.concurrent.*;

public class Debouncer<K> {
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<K, ScheduledFuture<?>> tasks;
    private final int delayInMillis;

    public Debouncer(int delayInMillis) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.tasks = new ConcurrentHashMap<>();
        this.delayInMillis = delayInMillis;
    }

    public void debounce(K key, Runnable action) {
        ScheduledFuture<?> existingTask = tasks.get(key);

        // Cancel the previous task if it exists
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        // Schedule a new task
        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            try {
                action.run();
            } finally {
                tasks.remove(key);
            }
        }, delayInMillis, TimeUnit.MILLISECONDS);

        tasks.put(key, newTask);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
