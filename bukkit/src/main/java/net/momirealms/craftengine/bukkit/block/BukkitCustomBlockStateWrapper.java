package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.AbstractBlockStateWrapper;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

public class BukkitCustomBlockStateWrapper extends AbstractBlockStateWrapper {

    public BukkitCustomBlockStateWrapper(Object blockState, int registryId) {
        super(blockState, registryId);
    }

    @Override
    public Key ownerId() {
        return getImmutableBlockState().map(state -> state.owner().value().id()).orElseGet(() -> BlockStateUtils.getBlockOwnerIdFromState(super.blockState));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String propertyName) {
        return (T) getImmutableBlockState().map(state -> {
            Property<?> property = state.owner().value().getProperty(propertyName);
            if (property == null)
                return null;
            return state.getNullable(property);
        }).orElse(null);
    }

    @Override
    public BlockStateWrapper withProperty(String propertyName, String propertyValue) {
        Optional<ImmutableBlockState> immutableBlockState = getImmutableBlockState();
        if (immutableBlockState.isPresent()) {
            Property<?> property = immutableBlockState.get().owner().value().getProperty(propertyName);
            if (property != null) {
                Comparable<?> value = property.valueByName(propertyValue);
                if (value != null) {
                    return ImmutableBlockState.with(immutableBlockState.get(), property, value).customBlockState();
                }
            }
        }
        return this;
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return getImmutableBlockState().map(state -> state.owner().value().getProperty(propertyName) != null).orElse(false);
    }

    @Override
    public String getAsString() {
        return getImmutableBlockState().map(ImmutableBlockState::toString).orElseGet(() -> BlockStateUtils.fromBlockData(super.blockState).getAsString());
    }

    public Optional<ImmutableBlockState> getImmutableBlockState() {
        return BlockStateUtils.getOptionalCustomBlockState(super.blockState);
    }
}
