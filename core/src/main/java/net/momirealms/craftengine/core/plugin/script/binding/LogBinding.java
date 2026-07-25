package net.momirealms.craftengine.core.plugin.script.binding;

import net.momirealms.craftengine.core.plugin.Plugin;

public final class LogBinding implements ScriptBinding {
    private final Plugin plugin;

    public LogBinding(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "log";
    }

    @Override
    public Object value() {
        return this;
    }

    public void info(String message) {
        this.plugin.logger().info(message);
    }

    public void warn(String message) {
        this.plugin.logger().warn(message);
    }

    public void severe(String message) {
        this.plugin.logger().error(message);
    }

    public void error(String message) {
        this.plugin.logger().error(message);
    }
}
