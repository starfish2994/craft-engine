package net.momirealms.craftengine.core.plugin.context.text;

import net.momirealms.craftengine.core.plugin.context.Context;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface StringTag {

    @Nullable
    Object resolve(String[] args, Context context);

    default StringTag precompile(String[] args) {
        return this;
    }

    static String requireArg(String[] args, int index, String message) {
        if (index >= args.length) {
            throw new IllegalArgumentException(message);
        }
        return args[index];
    }
}
