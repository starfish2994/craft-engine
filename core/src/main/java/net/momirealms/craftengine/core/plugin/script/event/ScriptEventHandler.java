package net.momirealms.craftengine.core.plugin.script.event;

@FunctionalInterface
public interface ScriptEventHandler {

    void handle(Object event);
}
