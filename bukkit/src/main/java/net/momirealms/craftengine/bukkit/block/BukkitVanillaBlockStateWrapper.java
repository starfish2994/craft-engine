package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.AbstractBlockStateWrapper;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.util.Key;

public class BukkitVanillaBlockStateWrapper extends AbstractBlockStateWrapper {
    private final StatePropertyAccessor accessor;

    public BukkitVanillaBlockStateWrapper(Object blockState, int registryId) {
        super(blockState, registryId);
        this.accessor = FastNMS.INSTANCE.createStatePropertyAccessor(blockState);
    }

    @Override
    public Key ownerId() {
        return BlockStateUtils.getBlockOwnerIdFromState(super.blockState);
    }

    @Override
    public <T> T getProperty(String propertyName) {
        return this.accessor.getPropertyValue(propertyName);
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return this.accessor.hasProperty(propertyName);
    }

    @Override
    public String getAsString() {
        return BlockStateUtils.fromBlockData(super.blockState).getAsString();
    }
}
