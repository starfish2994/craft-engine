package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.CanBeReplacedBlockBehavior;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;

import java.util.List;

public final class StackableBlockBehavior extends BukkitBlockBehavior implements CanBeReplacedBlockBehavior {
    public static final BlockBehaviorFactory<StackableBlockBehavior> FACTORY = new Factory();
    public final IntegerProperty amountProperty;
    public final List<Key> items;
    public final String propertyName;

    private StackableBlockBehavior(BlockDefinition block,
                                   IntegerProperty amountProperty,
                                   List<Key> items,
                                   String propertyName) {
        super(block);
        this.amountProperty = amountProperty;
        this.items = items;
        this.propertyName = propertyName;
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context, ImmutableBlockState state) {
        if (super.canBeReplaced(context, state)) {
            return true;
        }
        if (context.isSecondaryUseActive()) {
            return false;
        }
        Item item = context.getItem();
        if (ItemUtils.isEmpty(item)) {
            return false;
        }
        if (!this.items.contains(item.id())) {
            return false;
        }
        Property<?> property = state.owner().value().getProperty(this.propertyName);
        if (property == null || property.valueClass() != Integer.class) {
            return false;
        }
        return (Integer) state.get(property) < this.amountProperty.max;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Object world = context.getLevel().serverWorld();
        Object pos = LocationUtils.toBlockPos(context.getClickedPos());
        ImmutableBlockState blockState = BlockStateUtils.getOptionalCustomBlockState(BlockGetterProxy.INSTANCE.getBlockState(world, pos)).orElse(null);
        if (blockState == null) {
            return state;
        }
        Property<?> property = blockState.owner().value().getProperty(this.propertyName);
        if (property == null || property.valueClass() != Integer.class) {
            return state;
        }
        return blockState.cycle(property);
    }

    private static class Factory implements BlockBehaviorFactory<StackableBlockBehavior> {
        private static final String[] ITEMS = new String[] {"items", "item"};

        @Override
        public StackableBlockBehavior create(BlockDefinition block, ConfigSection section) {
            String propertyName = section.getString("property", "amount");
            return new StackableBlockBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(section.path(), block, propertyName, Integer.class),
                    section.getList(ITEMS, ConfigValue::getAsIdentifier),
                    propertyName
            );
        }
    }
}
