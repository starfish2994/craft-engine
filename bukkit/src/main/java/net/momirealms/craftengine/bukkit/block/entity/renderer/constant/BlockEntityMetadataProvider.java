package net.momirealms.craftengine.bukkit.block.entity.renderer.constant;

import net.momirealms.craftengine.core.block.entity.render.tint.BlockEntityTintSource;
import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface BlockEntityMetadataProvider {

    List<Object> apply(Player player, @Nullable BlockEntityTintSource tintSource, boolean force);
}
