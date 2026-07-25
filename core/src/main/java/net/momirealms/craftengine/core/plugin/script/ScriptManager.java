package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.script.binding.ScriptBinding;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface ScriptManager {

    Optional<ScriptFile> script(Key id);

    void load();

    void unload();

    void disable();

    boolean isAvailable();

    void registerBinding(ScriptBinding binding);

    Optional<ScriptFile> script(String id);

    @Nullable
    Object invoke(String id, String function, Context context, Map<String, Object> extras);

    boolean test(String id, String function, Context context, Map<String, Object> extras, boolean def);
}
