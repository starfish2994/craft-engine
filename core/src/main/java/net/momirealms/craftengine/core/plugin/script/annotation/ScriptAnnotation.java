package net.momirealms.craftengine.core.plugin.script.annotation;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ScriptAnnotation(String name, List<String> positional, Map<String, Object> named, String rawArgs) {

    @Nullable
    public String positional(int index) {
        return index < this.positional.size() ? this.positional.get(index) : null;
    }

    @Nullable
    public Object option(String key) {
        return this.named.get(key);
    }

    public boolean boolOption(String key, boolean def) {
        Object value = this.named.get(key);
        return value instanceof Boolean b ? b : def;
    }

    @Nullable
    public String stringOption(String key) {
        Object value = this.named.get(key);
        return value != null ? value.toString() : null;
    }
}
