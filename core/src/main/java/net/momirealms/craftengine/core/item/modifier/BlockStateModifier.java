package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.CustomBlockStateWrapper;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BlockStateModifier<I> implements SimpleNetworkItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private static final Object[] NBT_PATH = new Object[]{"BlockStateTag"};
    private final LazyReference<Map<String, String>> wrapper;

    public BlockStateModifier(LazyReference<Map<String, String>> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public Key type() {
        return ItemDataModifiers.BLOCK_STATE;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.blockState(this.wrapper.get());
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "BlockStateTag";
    }

    @Override
    public Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.BLOCK_STATE;
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @Override
        public ItemDataModifier<I> create(Object arg) {
            if (arg instanceof Map<?, ?> map) {
                Map<String, String> properties = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    properties.put(entry.getKey().toString(), entry.getValue().toString());
                }
                return new BlockStateModifier<>(LazyReference.lazyReference(() -> properties));
            } else {
                String stateString = arg.toString();
                return new BlockStateModifier<>(LazyReference.lazyReference(() -> {
                    BlockStateWrapper blockState = CraftEngine.instance().blockManager().createBlockState(stateString);
                    if (blockState instanceof CustomBlockStateWrapper customBlockStateWrapper) {
                        blockState = customBlockStateWrapper.visualBlockState();
                    }
                    if (blockState != null) {
                        Map<String, String> properties = new HashMap<>(4);
                        for (String property : blockState.getPropertyNames()) {
                            Object value = blockState.getProperty(property);
                            properties.put(property, String.valueOf(value).toLowerCase(Locale.ROOT));
                        }
                        return properties;
                    }
                    return Collections.emptyMap();
                }));
            }
        }
    }
}
