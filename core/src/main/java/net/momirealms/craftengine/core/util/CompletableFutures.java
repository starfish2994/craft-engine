package net.momirealms.craftengine.core.util;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class CompletableFutures {
    private CompletableFutures() {}

    /** Processes every item with a bounded number of tasks, reporting failures after all items finish. */
    public static <T> CompletableFuture<Void> forEachAsync(List<T> items, Consumer<? super T> action,
                                                          int parallelism, Executor executor) {
        if (parallelism < 1) throw new IllegalArgumentException("parallelism must be positive");
        int workers = Math.min(items.size(), parallelism);
        CompletableFuture<?>[] futures = new CompletableFuture<?>[workers];
        AtomicInteger next = new AtomicInteger();
        for (int worker = 0; worker < workers; worker++) {
            futures[worker] = CompletableFuture.runAsync(() -> {
                Throwable failure = null;
                int index;
                while ((index = next.getAndIncrement()) < items.size()) {
                    try {
                        action.accept(items.get(index));
                    } catch (Throwable e) {
                        // A failed item must not prevent the remaining items from running.
                        if (failure == null) failure = e;
                    }
                }
                if (failure != null) throw new CompletionException(failure);
            }, executor);
        }
        return CompletableFuture.allOf(futures);
    }

    public static <T extends CompletableFuture<?>> Collector<T, ImmutableList.Builder<T>, CompletableFuture<Void>> collector() {
        return Collector.of(
                ImmutableList.Builder::new,
                ImmutableList.Builder::add,
                (l, r) -> l.addAll(r.build()),
                builder -> allOf(builder.build())
        );
    }

    public static CompletableFuture<Void> allOf(Stream<? extends CompletableFuture<?>> futures) {
        CompletableFuture<?>[] arr = futures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(arr);
    }

    public static CompletableFuture<Void> allOf(Collection<? extends CompletableFuture<?>> futures) {
        CompletableFuture<?>[] arr = futures.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(arr);
    }
}
