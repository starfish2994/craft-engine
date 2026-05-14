package net.momirealms.craftengine.core.plugin.scheduler;

public final class DummyTask implements SchedulerTask {
    public static final SchedulerTask INSTANCE = new DummyTask();

    private DummyTask() {}

    @Override
    public void cancel() {
    }

    @Override
    public boolean cancelled() {
        return true;
    }
}
