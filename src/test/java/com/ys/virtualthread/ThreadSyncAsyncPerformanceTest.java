package com.ys.virtualthread;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ThreadSyncAsyncPerformanceTest {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 4;
    private static final int TASK_COUNT = 1_000;
    private static final int TASK_DURATION_MILLIS = 100;

    @Test
    void 플랫폼_스레드_동기_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS, false);

        System.out.println("Platform Threads (Sync): " + actual.toMillis() + " ms");
    }

    @Test
    void 플랫폼_스레드_비동기_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS, true);

        System.out.println("Platform Threads (Async): " + actual.toMillis() + " ms");
    }

    @Test
    void 가상_스레드_동기_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS, false);

        System.out.println("Virtual Threads (Sync): " + actual.toMillis() + " ms");
    }

    @Test
    void 가상_스레드_비동기_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS, true);

        System.out.println("Virtual Threads (Async): " + actual.toMillis() + " ms");
    }

    private Duration measurePerformance(ExecutorService executor, int taskCount, int taskDurationMillis, boolean isAsync) throws InterruptedException {
        Instant start = Instant.now();

        if (isAsync) {
            submitAsyncTasks(executor, taskCount, taskDurationMillis);
        } {
            submitSyncTasks(executor, taskCount, taskDurationMillis);
        }

        shutdownExecutor(executor);

        return Duration.between(start, Instant.now());
    }

    private void submitSyncTasks(ExecutorService executor, int taskCount, int taskDurationMillis) {
        IntStream.range(0, taskCount)
                .forEach(i -> executor.submit(() -> simulateIoOperation(taskDurationMillis)));
    }

    private void submitAsyncTasks(ExecutorService executor, int taskCount, int taskDurationMillis) {
        CompletableFuture<?>[] futures = IntStream.range(0, taskCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> simulateIoOperation(taskDurationMillis), executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void simulateIoOperation(int durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }
}
