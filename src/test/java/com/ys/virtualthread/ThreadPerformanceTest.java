package com.ys.virtualthread;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ThreadPerformanceTest {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 4;
    private static final int TASK_COUNT = 1_000;
    private static final int TASK_DURATION_MILLIS = 100;

    @Test
    void 플랫폼_스레드_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS);

        System.out.println("Platform Threads: " + actual.toMillis() + " ms");
    }

    @Test
    void 가상_스레드_퍼포먼스_테스트() throws InterruptedException{
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Duration actual = measurePerformance(executor, TASK_COUNT, TASK_DURATION_MILLIS);

        System.out.println("Virtual Threads: " + actual.toMillis() + " ms");
    }

    private Duration measurePerformance(ExecutorService executor, int taskCount, int taskDurationMillis) throws InterruptedException {
        Instant start = Instant.now();

        submitTasks(executor, taskCount, taskDurationMillis);

        shutdownExecutor(executor);

        return Duration.between(start, Instant.now());
    }

    private void submitTasks(ExecutorService executor, int taskCount, int taskDurationMillis) {
        IntStream.range(0, taskCount)
                .forEach(i -> executor.submit(() -> simulateIoOperation(taskDurationMillis)));
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
