package net.momirealms.craftengine.core.plugin.scheduler;

import org.jetbrains.annotations.NotNull;

public final class DummyRegionExecutor<T> implements RegionExecutor<T> {

    @Override
    public void run(Runnable runnable, T world, int x, int z) {
    }

    @Override
    public void runDelayed(Runnable runnable, T world, int x, int z) {
    }

    @Override
    public SchedulerTask runAsyncRepeating(Runnable runnable, long delay, long period) {
        return DummyTask.INSTANCE;
    }

    @Override
    public SchedulerTask runAsyncLater(Runnable runnable, long delay) {
        return DummyTask.INSTANCE;
    }

    @Override
    public SchedulerTask runLater(Runnable runnable, long delay, T world, int x, int z) {
        return DummyTask.INSTANCE;
    }

    @Override
    public SchedulerTask runRepeating(Runnable runnable, long delay, long period, T world, int x, int z) {
        return DummyTask.INSTANCE;
    }

    @Override
    public void execute(@NotNull Runnable command) {
    }
}
