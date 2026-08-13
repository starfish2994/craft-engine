package net.momirealms.craftengine.core.attribute.derived;

import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Function;

public interface DerivedValue {

    default void bind(Function<Key, Attribute> resolver) {
    }

    double evaluate(Function<Attribute, Double> resolver);
}
