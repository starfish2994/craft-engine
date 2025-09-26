package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.block.behavior.UnsafeCompositeBlockBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistries;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.SoundUtils;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.behavior.AbstractBlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.behavior.EmptyBlockBehavior;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BukkitCustomBlock extends AbstractCustomBlock {

    private BukkitCustomBlock(
            @NotNull Key id,
            @NotNull Holder.Reference<CustomBlock> holder,
            @NotNull Map<String, Property<?>> properties,
            @NotNull Map<String, BlockStateAppearance> appearances,
            @NotNull Map<String, BlockStateVariant> variantMapper,
            @NotNull BlockSettings settings,
            @NotNull Map<EventTrigger, List<Function<PlayerOptionalContext>>> events,
            @Nullable List<Map<String, Object>> behavior,
            @Nullable LootTable<?> lootTable
    ) {
        super(id, holder, properties, appearances, variantMapper, settings, events, behavior, lootTable);
    }

    @Override
    protected BlockBehavior setupBehavior(List<Map<String, Object>> behaviorConfig) {
        if (behaviorConfig == null || behaviorConfig.isEmpty()) {
            return new EmptyBlockBehavior();
        } else if (behaviorConfig.size() == 1) {
            return BlockBehaviors.fromMap(this, behaviorConfig.getFirst());
        } else {
            List<AbstractBlockBehavior> behaviors = new ArrayList<>();
            for (Map<String, Object> config : behaviorConfig) {
                behaviors.add((AbstractBlockBehavior) BlockBehaviors.fromMap(this, config));
            }
            return new UnsafeCompositeBlockBehavior(this, behaviors);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public LootTable<ItemStack> lootTable() {
        return (LootTable<ItemStack>) super.lootTable();
    }

    public static Builder builder(Key id) {
        return new BuilderImpl(id);
    }

    public static class BuilderImpl implements Builder {
        protected final Key id;
        protected Map<String, Property<?>> properties;
        protected Map<String, BlockStateAppearance> appearances;
        protected Map<String, BlockStateVariant> variantMapper;
        protected BlockSettings settings;
        protected List<Map<String, Object>> behavior;
        protected LootTable<?> lootTable;
        protected Map<EventTrigger, List<Function<PlayerOptionalContext>>> events;

        public BuilderImpl(Key id) {
            this.id = id;
        }

        @Override
        public Builder events(Map<EventTrigger, List<Function<PlayerOptionalContext>>> events) {
            this.events = events;
            return this;
        }

        @Override
        public Builder appearances(Map<String, BlockStateAppearance> appearances) {
            this.appearances = appearances;
            return this;
        }

        @Override
        public Builder behavior(List<Map<String, Object>> behavior) {
            this.behavior = behavior;
            return this;
        }

        @Override
        public Builder lootTable(LootTable<?> lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        @Override
        public Builder properties(Map<String, Property<?>> properties) {
            this.properties = properties;
            return this;
        }

        @Override
        public Builder settings(BlockSettings settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public Builder variantMapper(Map<String, BlockStateVariant> variantMapper) {
            this.variantMapper = variantMapper;
            return this;
        }

        @Override
        public @NotNull CustomBlock build() {
            // create or get block holder
            Holder.Reference<CustomBlock> holder = ((WritableRegistry<CustomBlock>) BuiltInRegistries.BLOCK).getOrRegisterForHolder(ResourceKey.create(BuiltInRegistries.BLOCK.key().location(), this.id));
            return new BukkitCustomBlock(this.id, holder, this.properties, this.appearances, this.variantMapper, this.settings, this.events, this.behavior, this.lootTable);
        }
    }
}
