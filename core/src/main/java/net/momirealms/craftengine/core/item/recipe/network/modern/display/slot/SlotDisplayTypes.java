package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.function.BiFunction;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SlotDisplayTypes {
    private SlotDisplayTypes() {}

    public static final Key EMPTY = Key.of("empty");
    public static final Key ANY_FUEL = Key.of("any_fuel");
    public static final Key ITEM = Key.of("item");
    public static final Key ITEM_STACK = Key.of("item_stack");
    public static final Key TAG = Key.of("tag");
    public static final Key SMITHING_TRIM = Key.of("smithing_trim");
    public static final Key WITH_REMAINDER = Key.of("with_remainder");
    public static final Key COMPOSITE = Key.of("composite");

    public static void init() {
    }

    static {
        register(EMPTY, new SlotDisplay.Type(createReaderFunction(EmptySlotDisplay::read)));
        register(ANY_FUEL, new SlotDisplay.Type(createReaderFunction(AnyFuelDisplay::read)));
        register(ITEM, new SlotDisplay.Type(createReaderFunction(ItemSlotDisplay::read)));
        register(ITEM_STACK, new SlotDisplay.Type(createReaderFunction(ItemStackSlotDisplay::read)));
        register(TAG, new SlotDisplay.Type(createReaderFunction(TagSlotDisplay::read)));
        register(SMITHING_TRIM, new SlotDisplay.Type(createReaderFunction(SmithingTrimDemoSlotDisplay::read)));
        register(WITH_REMAINDER, new SlotDisplay.Type(createReaderFunction(WithRemainderSlotDisplay::read)));
        register(COMPOSITE, new SlotDisplay.Type(createReaderFunction(CompositeSlotDisplay::read)));
    }

    private static <I> BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<Item<I>>, SlotDisplay<I>> createReaderFunction(
            BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader, SlotDisplay> function) {
        return (BiFunction) function;
    }
    
    public static <I> void register(Key key, SlotDisplay.Type<I> type) {
        ((WritableRegistry<SlotDisplay.Type<?>>) BuiltInRegistries.SLOT_DISPLAY_TYPE)
                .register(ResourceKey.create(Registries.SLOT_DISPLAY_TYPE.location(), key), type);
    }
}
