package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;

public final class ScriptTaskHandle {
    private final ScriptFile script;
    private SchedulerTask delegate;

    public ScriptTaskHandle(ScriptFile script) {
        this.script = script;
    }

    public void init(SchedulerTask delegate) {
        this.delegate = delegate;
        this.script.trackTask(this);
    }

    public void cancel() {
        if (this.delegate != null) {
            this.delegate.cancel();
        }
        this.script.untrackTask(this);
    }

    public boolean cancelled() {
        return this.delegate == null || this.delegate.cancelled();
    }
}
