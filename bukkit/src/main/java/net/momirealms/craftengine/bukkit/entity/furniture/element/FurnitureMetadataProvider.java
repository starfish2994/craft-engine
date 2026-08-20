package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.element.tint.FurnitureTintSource;
import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface FurnitureMetadataProvider {

    List<Object> apply(Player player, @Nullable FurnitureTintSource tintSource, boolean force);
}
