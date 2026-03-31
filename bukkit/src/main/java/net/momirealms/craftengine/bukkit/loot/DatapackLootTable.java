package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.Lootable;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class DatapackLootTable implements Lootable {

    public LazyReference<Object> minecraftLootTable;

    @Override
    public List<Item> getRandomItems(LootContext context) {
        return List.of();
    }

    @Override
    public List<Item> getRandomItems(ContextHolder parameters, World world) {
        return List.of();
    }

    @Override
    public List<Item> getRandomItems(ContextHolder parameters, World world, @Nullable Player player) {
        return List.of();
    }

    @Override
    public void getRandomItems(LootContext context, Consumer<Item> lootConsumer) {

    }

}
