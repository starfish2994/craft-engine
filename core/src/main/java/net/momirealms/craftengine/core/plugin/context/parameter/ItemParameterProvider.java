package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.processor.RandomValuesProcessor;
import net.momirealms.craftengine.core.plugin.context.ChainParameterProvider;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.NamedRandoms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ItemParameterProvider implements ChainParameterProvider<Item> {
    public static final ItemParameterProvider INSTANCE = new ItemParameterProvider();
    private static final Map<ContextKey<?>, Function<Item, Object>> CONTEXT_FUNCTIONS = new HashMap<>();

    static {
        CONTEXT_FUNCTIONS.put(DirectContextParameters.ID, Item::id);
        CONTEXT_FUNCTIONS.put(DirectContextParameters.CUSTOM_MODEL_DATA, i -> i.customModelData().orElse(null));
        CONTEXT_FUNCTIONS.put(DirectContextParameters.IS_CUSTOM, Item::isCustomItem);
        CONTEXT_FUNCTIONS.put(DirectContextParameters.COUNT, Item::count);
        CONTEXT_FUNCTIONS.put(DirectContextParameters.IS_BLOCK_ITEM, Item::isBlockItem);
        CONTEXT_FUNCTIONS.put(DirectContextParameters.RANDOM, item -> item.getCustomData(NamedRandoms.class, RandomValuesProcessor.TAG_PATH));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter, Item item) {
        return (Optional<T>) Optional.ofNullable(CONTEXT_FUNCTIONS.get(parameter)).map(f -> f.apply(item));
    }
}