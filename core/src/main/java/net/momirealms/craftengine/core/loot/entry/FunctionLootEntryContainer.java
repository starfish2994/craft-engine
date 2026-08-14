package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;

import java.util.List;
import java.util.function.Consumer;

public final class FunctionLootEntryContainer extends AbstractSingleLootEntryContainer {
    public static final LootEntryContainerFactory<FunctionLootEntryContainer> FACTORY = new Factory();
    private final Function<Context> function;

    private FunctionLootEntryContainer(List<Condition<LootContext>> conditions, List<LootFunction> lootFunctions, int weight, int quality, Function<Context> function) {
        super(conditions, lootFunctions, weight, quality);
        this.function = function;
    }

    @Override
    protected void createItem(Consumer<Item> lootConsumer, LootContext context) {
        this.function.run(context);
    }

    private static class Factory implements LootEntryContainerFactory<FunctionLootEntryContainer> {

        @Override
        public FunctionLootEntryContainer create(ConfigSection section) {
            return new FunctionLootEntryContainer(
                    section.getList(ConfigKeys.of("condition(s)"), CommonConditions::fromConfig),
                    section.getList("functions", LootFunctions::fromConfig),
                    section.getInt("weight", 1),
                    section.getInt("quality"),
                    CommonFunctions.fromConfig(section.getNonNullValue("run", ConfigConstants.ARGUMENT_ANY))
            );
        }
    }
}
