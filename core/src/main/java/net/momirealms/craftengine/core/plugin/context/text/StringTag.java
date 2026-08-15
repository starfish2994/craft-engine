package net.momirealms.craftengine.core.plugin.context.text;

import net.momirealms.craftengine.core.plugin.context.Context;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface StringTag {

    @Nullable
    String resolve(String[] args, Context context);
}
