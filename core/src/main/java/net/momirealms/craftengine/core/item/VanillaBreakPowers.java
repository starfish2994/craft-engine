package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class VanillaBreakPowers {
    private static final Map<Key, Integer> DEFAULT_BREAK_POWERS = Map.ofEntries(
            Map.entry(ItemKeys.WOODEN_PICKAXE, 1),
            Map.entry(ItemKeys.WOODEN_AXE, 1),
            Map.entry(ItemKeys.WOODEN_SHOVEL, 1),
            Map.entry(ItemKeys.WOODEN_HOE, 1),
            Map.entry(ItemKeys.GOLDEN_PICKAXE, 1),
            Map.entry(ItemKeys.GOLDEN_AXE, 1),
            Map.entry(ItemKeys.GOLDEN_SHOVEL, 1),
            Map.entry(ItemKeys.GOLDEN_HOE, 1),
            Map.entry(ItemKeys.STONE_PICKAXE, 2),
            Map.entry(ItemKeys.STONE_AXE, 2),
            Map.entry(ItemKeys.STONE_SHOVEL, 2),
            Map.entry(ItemKeys.STONE_HOE, 2),
            Map.entry(ItemKeys.COPPER_PICKAXE, 2),
            Map.entry(ItemKeys.COPPER_AXE, 2),
            Map.entry(ItemKeys.COPPER_SHOVEL, 2),
            Map.entry(ItemKeys.COPPER_HOE, 2),
            Map.entry(ItemKeys.IRON_PICKAXE, 3),
            Map.entry(ItemKeys.IRON_AXE, 3),
            Map.entry(ItemKeys.IRON_SHOVEL, 3),
            Map.entry(ItemKeys.IRON_HOE, 3),
            Map.entry(ItemKeys.DIAMOND_PICKAXE, 4),
            Map.entry(ItemKeys.DIAMOND_AXE, 4),
            Map.entry(ItemKeys.DIAMOND_SHOVEL, 4),
            Map.entry(ItemKeys.DIAMOND_HOE, 4),
            Map.entry(ItemKeys.NETHERITE_PICKAXE, 5),
            Map.entry(ItemKeys.NETHERITE_AXE, 5),
            Map.entry(ItemKeys.NETHERITE_SHOVEL, 5),
            Map.entry(ItemKeys.NETHERITE_HOE, 5)
    );

    private VanillaBreakPowers() {}

    public static int breakPower(Key vanillaItemId) {
        Integer override = Config.itemBreakPowerOverrides().get(vanillaItemId);
        if (override != null) return override;
        return DEFAULT_BREAK_POWERS.getOrDefault(vanillaItemId, 0);
    }
}
