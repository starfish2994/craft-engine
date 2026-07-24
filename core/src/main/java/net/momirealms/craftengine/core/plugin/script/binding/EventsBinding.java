package net.momirealms.craftengine.core.plugin.script.binding;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class EventsBinding {
    private final ScriptManagerImpl manager;
    private final ScriptFile script;

    public EventsBinding(ScriptManagerImpl manager, ScriptFile script) {
        this.manager = manager;
        this.script = script;
    }

    public void subscribe(Class<?> eventClass, ScriptEventHandler handler) {
        subscribe(eventClass, handler, null);
    }

    public void subscribe(Class<?> eventClass, ScriptEventHandler handler, @Nullable Map<String, Object> options) {
        this.manager.subscribeEvent(this.script, eventClass, handler, options);
    }
}
