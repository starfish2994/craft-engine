package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.LootTableReference;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class LootTableLootEntryContainer extends AbstractSingleLootEntryContainer {
    public static final LootEntryContainerFactory<LootTableLootEntryContainer> FACTORY = new Factory();
    private static final ThreadLocal<Set<Key>> ACTIVE_TABLES = ThreadLocal.withInitial(HashSet::new);

    private final Key tableId;
    private final LootTableReference reference;

    private LootTableLootEntryContainer(List<Condition<LootContext>> conditions, List<LootFunction> lootFunctions, int weight, int quality, Key tableId) {
        super(conditions, lootFunctions, weight, quality);
        this.tableId = tableId;
        this.reference = CraftEngine.instance().lootManager().createReference(tableId);
    }

    @Override
    protected void createItem(Consumer<Item> lootConsumer, LootContext context) {
        Set<Key> activeTables = ACTIVE_TABLES.get();
        if (!activeTables.add(this.tableId)) {
            CraftEngine.instance().logger().warn("Detected circular loot_table reference '" + this.tableId + "', skipping");
            return;
        }
        try {
            this.reference.getRandomItems(context, lootConsumer);
        } finally {
            activeTables.remove(this.tableId);
            if (activeTables.isEmpty()) {
                ACTIVE_TABLES.remove();
            }
        }
    }

    private static class Factory implements LootEntryContainerFactory<LootTableLootEntryContainer> {
        private static final String[] ID = ConfigKeys.of("id|table|name");

        @Override
        public LootTableLootEntryContainer create(ConfigSection section) {
            return new LootTableLootEntryContainer(
                    section.getList(ConfigKeys.of("condition(s)"), CommonConditions::fromConfig),
                    section.getList("functions", LootFunctions::fromConfig),
                    section.getInt("weight", 1),
                    section.getInt("quality"),
                    section.getNonNullIdentifier(ID)
            );
        }
    }
}
