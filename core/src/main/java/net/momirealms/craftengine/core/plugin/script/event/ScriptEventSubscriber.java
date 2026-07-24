package net.momirealms.craftengine.core.plugin.script.event;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ScriptEventSubscriber {

    void subscribe(ScriptFile script, Class<?> eventClass, ScriptEventHandler handler, @Nullable Map<String, Object> options);
}
