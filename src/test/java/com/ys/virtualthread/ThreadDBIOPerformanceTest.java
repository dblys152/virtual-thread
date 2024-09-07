package com.ys.virtualthread;

import com.ys.virtualthread.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Transactional
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DatabaseConfig.class})
public class ThreadDBIOPerformanceTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 4;
    private static final int TASK_COUNT = 1_000;
    private static final String SELECT_QUERY = "SELECT PG_SLEEP(0.1)";

    @Test
    void 플랫폼_스레드_DB_IO_퍼포먼스_테스트() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Duration actual = measurePerformance(executor, TASK_COUNT);

        System.out.println("Platform Threads: " + actual.toMillis() + " ms");
    }

    @Test
    void 가상_스레드_DB_IO_퍼포먼스_테스트() throws InterruptedException{
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Duration actual = measurePerformance(executor, TASK_COUNT);

        System.out.println("Virtual Threads: " + actual.toMillis() + " ms");
    }

    private Duration measurePerformance(ExecutorService executor, int taskCount) throws InterruptedException {
        Instant start = Instant.now();

        submitTasks(executor, taskCount);

        shutdownExecutor(executor);

        return Duration.between(start, Instant.now());
    }

    private void submitTasks(ExecutorService executor, int taskCount) {
        IntStream.range(0, taskCount)
                .forEach(i -> executor.submit(this::simulateIoOperation));
    }

    private void simulateIoOperation() {
        jdbcTemplate.queryForObject(SELECT_QUERY, String.class);
    }

    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }
}
