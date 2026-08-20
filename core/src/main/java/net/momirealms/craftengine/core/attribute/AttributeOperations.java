package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public final class AttributeOperations {
    private AttributeOperations() {}

    public static final Key ADD_VALUE_ID = Key.minecraft("add_value");
    public static final Key ADD_MULTIPLIED_BASE_ID = Key.minecraft("add_multiplied_base");
    public static final Key ADD_MULTIPLIED_TOTAL_ID = Key.minecraft("add_multiplied_total");

    public static final AttributeOperation ADD_VALUE = AttributeOperation.of(ADD_VALUE_ID,
            (base, current, amount) -> current + amount);
    public static final AttributeOperation ADD_MULTIPLIED_BASE = AttributeOperation.of(ADD_MULTIPLIED_BASE_ID,
            (base, current, amount) -> current + base * amount);
    public static final AttributeOperation ADD_MULTIPLIED_TOTAL = AttributeOperation.of(ADD_MULTIPLIED_TOTAL_ID,
            (base, current, amount) -> current * (1 + amount));

    public static final List<AttributeOperation> DEFAULT_PIPELINE = List.of(ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL);
}
