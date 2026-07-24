package net.momirealms.craftengine.core.plugin.script.binding;

import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;

import java.util.concurrent.TimeUnit;

public final class SchedulerBinding implements ScriptBinding {
    private final Plugin plugin;

    public SchedulerBinding(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "scheduler";
    }

    @Override
    public Object value() {
        return this;
    }

    public void sync(Runnable task) {
        this.plugin.scheduler().platform().run(task);
    }

    public void async(Runnable task) {
        this.plugin.scheduler().async().execute(task);
    }

    public SchedulerTask later(Runnable task, long delayTicks) {
        return this.plugin.scheduler().platform().runLater(task, delayTicks);
    }

    public SchedulerTask timer(Runnable task, long delayTicks, long periodTicks) {
        return this.plugin.scheduler().platform().runRepeating(task, delayTicks, periodTicks);
    }

    public SchedulerTask asyncLater(Runnable task, long delayTicks) {
        return this.plugin.scheduler().asyncLater(task, delayTicks * 50, TimeUnit.MILLISECONDS);
    }

    public SchedulerTask asyncTimer(Runnable task, long delayTicks, long periodTicks) {
        return this.plugin.scheduler().asyncRepeating(task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
    }
}
