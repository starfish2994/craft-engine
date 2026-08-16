package net.momirealms.craftengine.core.plugin.context;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.util.random.RandomUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleSupplier;

public final class NamedRandoms implements ChainParameterSource {
    public final Map<String, Double> values;

    public NamedRandoms() {
        this.values = new Object2ObjectOpenHashMap<>(4);
    }

    public NamedRandoms(Map<String, Double> values) {
        this.values = values;
    }

    public double getOrRoll(String id) {
        return getOrRoll(id, () -> RandomUtils.generateRandomDouble(0, 1));
    }

    public double getOrRoll(String id, DoubleSupplier roll) {
        Double existing = this.values.get(id);
        if (existing != null) return existing;
        double value = roll.getAsDouble();
        this.values.put(id, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getParameter(ContextKey<T> key) {
        return (Optional<T>) Optional.of(getOrRoll(key.node()));
    }
}
