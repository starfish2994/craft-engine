package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public class MaxDamageModifier<I> implements SimpleNetworkItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final NumberProvider argument;

    public MaxDamageModifier(NumberProvider argument) {
        this.argument = argument;
    }

    @Override
    public Key type() {
        return ItemDataModifiers.MAX_DAMAGE;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.maxDamage(argument.getInt(context));
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.MAX_DAMAGE;
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @Override
        public ItemDataModifier<I> create(Object arg) {
            NumberProvider numberProvider = NumberProviders.fromObject(arg);
            return new MaxDamageModifier<>(numberProvider);
        }
    }
}
