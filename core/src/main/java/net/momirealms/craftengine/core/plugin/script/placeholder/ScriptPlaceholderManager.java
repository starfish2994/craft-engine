package net.momirealms.craftengine.core.plugin.script.placeholder;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import org.jetbrains.annotations.Nullable;

public interface ScriptPlaceholderManager {

    void register(ScriptFile script, String placeholder, String function, boolean relational);

    @Nullable
    String resolve(String params, @Nullable Object player);

    @Nullable
    String resolveRelational(String params, @Nullable Object one, @Nullable Object two);
}
