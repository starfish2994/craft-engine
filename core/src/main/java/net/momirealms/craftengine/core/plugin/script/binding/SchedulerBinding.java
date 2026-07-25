package net.momirealms.craftengine.core.plugin.script.binding;

import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptTaskHandle;

import java.util.function.Consumer;

public final class SchedulerBinding {
    private final Plugin plugin;
    private final ScriptFile script;

    public SchedulerBinding(Plugin plugin, ScriptFile script) {
        this.plugin = plugin;
        this.script = script;
    }

    public void sync(Runnable task) {
        this.plugin.scheduler().platform().run(task);
    }

    public void async(Runnable task) {
        this.plugin.scheduler().async().execute(task);
    }

    public ScriptTaskHandle later(Runnable task, long delayTicks) {
        ScriptTaskHandle handle = new ScriptTaskHandle(this.script);
        handle.init(this.plugin.scheduler().platform().runLater(wrapOnce(handle, task), delayTicks));
        return handle;
    }

    public ScriptTaskHandle timer(Consumer<ScriptTaskHandle> task, long delayTicks, long periodTicks) {
        ScriptTaskHandle handle = new ScriptTaskHandle(this.script);
        handle.init(this.plugin.scheduler().platform().runRepeating(() -> task.accept(handle), delayTicks, periodTicks));
        return handle;
    }

    public ScriptTaskHandle asyncLater(Runnable task, long delayTicks) {
        ScriptTaskHandle handle = new ScriptTaskHandle(this.script);
        handle.init(this.plugin.scheduler().platform().runAsyncLater(wrapOnce(handle, task), delayTicks));
        return handle;
    }

    public ScriptTaskHandle asyncTimer(Consumer<ScriptTaskHandle> task, long delayTicks, long periodTicks) {
        ScriptTaskHandle handle = new ScriptTaskHandle(this.script);
        handle.init(this.plugin.scheduler().platform().runAsyncRepeating(() -> task.accept(handle), delayTicks, periodTicks));
        return handle;
    }

    private Runnable wrapOnce(ScriptTaskHandle handle, Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                this.script.untrackTask(handle);
            }
        };
    }
}
