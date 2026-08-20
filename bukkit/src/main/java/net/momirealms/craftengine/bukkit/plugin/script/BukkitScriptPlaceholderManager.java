package net.momirealms.craftengine.bukkit.plugin.script;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.placeholder.ScriptPlaceholderManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BukkitScriptPlaceholderManager implements ScriptPlaceholderManager {
    private final Map<String, Entry> placeholders = new ConcurrentHashMap<>();
    private final Map<String, Entry> relationalPlaceholders = new ConcurrentHashMap<>();

    @Nullable
    private static Match match(Map<String, Entry> map, String params) {
        String lower = params.toLowerCase(Locale.ENGLISH);
        Entry exact = map.get(lower);
        if (exact != null) {
            return new Match(exact, "");
        }
        int idx = lower.length();
        while ((idx = lower.lastIndexOf('_', idx - 1)) > 0) {
            Entry entry = map.get(lower.substring(0, idx));
            if (entry != null) {
                return new Match(entry, params.substring(idx + 1));
            }
        }
        return null;
    }

    @Override
    public void register(ScriptFile script, String placeholder, String function, boolean relational) {
        Entry entry = new Entry(script, function);
        (relational ? this.relationalPlaceholders : this.placeholders).put(placeholder.toLowerCase(Locale.ENGLISH), entry);
        script.onUnload(() -> {
            this.placeholders.values().removeIf(e -> e == entry);
            this.relationalPlaceholders.values().removeIf(e -> e == entry);
        });
    }

    @Nullable
    @Override
    public String resolve(String params, @Nullable Object player) {
        Match match = match(this.placeholders, params);
        if (match == null) return null;
        Map<String, Object> injected = new HashMap<>();
        injected.put("args", match.args());
        if (player != null) injected.put("player", player);
        Object result = match.entry().script().invoke(match.entry().function(), injected, player, match.args());
        return result == null ? null : result.toString();
    }

    @Nullable
    @Override
    public String resolveRelational(String params, @Nullable Object one, @Nullable Object two) {
        Match match = match(this.relationalPlaceholders, params);
        if (match == null) return null;
        Map<String, Object> injected = new HashMap<>();
        injected.put("args", match.args());
        if (one != null) injected.put("one", one);
        if (two != null) injected.put("two", two);
        Object result = match.entry().script().invoke(match.entry().function(), injected, one, two, match.args());
        return result == null ? null : result.toString();
    }

    private record Entry(ScriptFile script, String function) {
    }

    private record Match(Entry entry, String args) {
    }
}
