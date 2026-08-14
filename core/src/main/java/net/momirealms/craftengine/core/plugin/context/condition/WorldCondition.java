package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class WorldCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> worlds;

    private WorldCondition(Set<String> worlds) {
        this.worlds = worlds;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<World> world = ctx.getOptionalParameter(DirectContextParameters.WORLD);
        if (world.isEmpty()) {
            world = ctx.getOptionalParameter(DirectContextParameters.POSITION).map(WorldPosition::world);
        }
        return world.isPresent() && this.worlds.contains(world.get().name());
    }

    public static <CTX extends Context> ConditionFactory<CTX, WorldCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, WorldCondition<CTX>> {
        private static final String[] WORLD = ConfigKeys.of("world(s)");

        @Override
        public WorldCondition<CTX> create(ConfigSection section) {
            return new WorldCondition<>(new HashSet<>(section.getStringList(WORLD)));
        }
    }
}
