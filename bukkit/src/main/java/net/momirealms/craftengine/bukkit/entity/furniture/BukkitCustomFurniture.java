package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.AbstractCustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.AnchorType;
import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureSettings;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BukkitCustomFurniture extends AbstractCustomFurniture {

    protected BukkitCustomFurniture(@NotNull Key id,
                                    @NotNull FurnitureSettings settings,
                                    @NotNull Map<AnchorType, Placement> placements,
                                    @NotNull Map<EventTrigger, List<Function<Context>>> events,
                                    @Nullable LootTable<?> lootTable) {
        super(id, settings, placements, events, lootTable);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static class BuilderImpl implements Builder {
        private Key id;
        private Map<AnchorType, Placement> placements;
        private FurnitureSettings settings;
        private Map<EventTrigger, List<Function<Context>>> events;
        private LootTable<?> lootTable;

        @Override
        public CustomFurniture build() {
            return new BukkitCustomFurniture(id, settings, placements, events, lootTable);
        }

        @Override
        public Builder id(Key id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder placement(Map<AnchorType, Placement> placements) {
            this.placements = placements;
            return this;
        }

        @Override
        public Builder settings(FurnitureSettings settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public Builder lootTable(LootTable<?> lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        @Override
        public Builder events(Map<EventTrigger, List<Function<Context>>> events) {
            this.events = events;
            return this;
        }
    }
}
