package net.momirealms.craftengine.core.util;

import java.util.StringJoiner;

public final class EnumUtils {
    private EnumUtils() {
    }

    public static String toString(Enum<?>[] enums) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Enum<?> e : enums) {
            joiner.add(e.name());
        }
        return joiner.toString();
    }
}
