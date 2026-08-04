package net.momirealms.craftengine.bukkit.compatibility.util;

import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class FurnitureResolver {
    private FurnitureResolver() {}

    /**
     * 依次尝试将实体解析为家具：元数据实体 -> 座位 -> 碰撞体
     */
    public static @Nullable BukkitFurniture resolve(@Nullable Entity entity) {
        if (entity == null) return null;
        BukkitFurniture furniture = CraftEngineFurniture.getLoadedFurnitureByMetaEntity(entity);
        if (furniture != null) return furniture;
        furniture = CraftEngineFurniture.getLoadedFurnitureBySeat(entity);
        if (furniture != null) return furniture;
        return CraftEngineFurniture.getLoadedFurnitureByCollider(entity);
    }
}
