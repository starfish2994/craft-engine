package net.momirealms.craftengine.core.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CompletableFuturesTest {
    @Test
    void emptyInputCompletesWithoutSubmittingWork() {
        var result = CompletableFutures.forEachAsync(List.of(), item -> fail("Unexpected item"),
                8, task -> fail("Unexpected task"));
        assertTrue(result.isDone());
        result.join();
    }

    @Test
    void limitsSubmittedTasksAndProcessesEveryItem() {
        List<Integer> items = IntStream.range(0, 10_000).boxed().toList();
        int[] visits = new int[items.size()];
        ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        var result = CompletableFutures.forEachAsync(items, item -> visits[item]++, 8, tasks::add);
        assertEquals(8, tasks.size());
        assertFalse(result.isDone());
        while (!tasks.isEmpty()) tasks.remove().run();
        result.join();
        for (int visit : visits) assertEquals(1, visit);
    }

    @Test
    void reportsFailureAfterProcessingRemainingItems() {
        int[] visits = new int[6];
        ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        IllegalStateException failure = new IllegalStateException("Bad image");
        var result = CompletableFutures.forEachAsync(List.of(0, 1, 2, 3, 4, 5), item -> {
            visits[item]++;
            if (item == 0) throw failure;
            if (item == 1) throw new AssertionError("Another bad image");
        }, 2, tasks::add);
        tasks.remove().run();
        assertFalse(result.isDone());
        tasks.remove().run();
        assertSame(failure, assertThrows(CompletionException.class, result::join).getCause());
        for (int visit : visits) assertEquals(1, visit);
    }

    @Test
    void parallelWorkersProcessEachItemExactlyOnceAndWaitForCompletion() throws Exception {
        List<Integer> items = IntStream.range(0, 10_000).boxed().toList();
        AtomicIntegerArray visits = new AtomicIntegerArray(items.size());
        CountDownLatch started = new CountDownLatch(4);
        CountDownLatch release = new CountDownLatch(1);
        try (ForkJoinPool pool = new ForkJoinPool(4)) {
            var result = CompletableFutures.forEachAsync(items, item -> {
                if (item < 4) {
                    started.countDown();
                    try {
                        if (!release.await(10, TimeUnit.SECONDS)) throw new AssertionError("Release timed out");
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
                visits.incrementAndGet(item);
            }, 4, pool);
            try {
                assertTrue(started.await(10, TimeUnit.SECONDS));
                assertFalse(result.isDone());
            } finally {
                release.countDown();
            }
            result.get(10, TimeUnit.SECONDS);
            for (int i = 0; i < visits.length(); i++) assertEquals(1, visits.get(i));
        }
    }
}
