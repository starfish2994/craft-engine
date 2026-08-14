package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class BiomeCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<Key> biomes;

    private BiomeCondition(Set<Key> biomes) {
        this.biomes = biomes;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<WorldPosition> position = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        return position.filter(worldPosition -> this.biomes.contains(worldPosition.getBiome())).isPresent();
    }

    public static <CTX extends Context> ConditionFactory<CTX, BiomeCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, BiomeCondition<CTX>> {
        private static final String[] BIOME = ConfigKeys.of("biome(s)");

        @Override
        public BiomeCondition<CTX> create(ConfigSection section) {
            return new BiomeCondition<>(new HashSet<>(section.getList(BIOME, ConfigValue::getAsIdentifier)));
        }
    }
}
