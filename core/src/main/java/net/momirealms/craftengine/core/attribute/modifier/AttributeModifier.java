package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public interface AttributeModifier {

    Key id();

    double amount(Context context);

    Key operation();

    boolean test(Context context);

    default int updateInterval() {
        return 0;
    }

    default boolean isDynamic() {
        return true;
    }
}
