package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.BlockEntityTypeKeys;
import net.momirealms.craftengine.core.block.entity.BlockEntityTypes;

public class BukkitBlockEntityTypes extends BlockEntityTypes {
    public static final BlockEntityType<SimpleStorageBlockEntity> SIMPLE_STORAGE = register(BlockEntityTypeKeys.SIMPLE_STORAGE, SimpleStorageBlockEntity::new);
}