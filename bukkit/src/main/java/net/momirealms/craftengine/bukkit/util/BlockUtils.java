package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MTagKeys;
import net.momirealms.craftengine.core.block.BlockStateWrapper;

public final class BlockUtils {
    private BlockUtils() {}

    public static boolean isExceptionForConnection(BlockStateWrapper state) {
        Object blockState = state.literalObject();
        return CoreReflections.clazz$LeavesBlock.isInstance(BlockStateUtils.getBlockOwner(blockState))
                || FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.BARRIER)
                || FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.CARVED_PUMPKIN)
                || FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.JACK_O_LANTERN)
                || FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.MELON)
                || FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.PUMPKIN)
                || FastNMS.INSTANCE.method$BlockStateBase$is(blockState, MTagKeys.Block$SHULKER_BOXES);
    }
}
