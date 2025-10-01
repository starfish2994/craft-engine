package net.momirealms.craftengine.bukkit.compatibility.item;

import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.drops.DropMetadataImpl;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MythicMobsSource implements ExternalItemSource<ItemStack> {
    private MythicBukkit mythicBukkit;

    @Override
    public String plugin() {
        return "mythicmobs";
    }

    @Nullable
    @Override
    public ItemStack build(String id, ItemBuildContext context) {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        return Optional.ofNullable(context.player())
                .map(p -> (Player) p.platformPlayer())
                .map(p -> {
                    AbstractPlayer target = BukkitAdapter.adapt(p);
                    SkillCaster caster = mythicBukkit.getSkillManager().getCaster(target);
                    DropMetadataImpl meta = new DropMetadataImpl(caster, target);
                    return mythicBukkit.getItemManager().getItem(id)
                            .map(i -> i.generateItemStack(meta, 1))
                            .map(BukkitAdapter::adapt)
                            .orElse(null);
                })
                .orElseGet(() -> mythicBukkit.getItemManager().getItemStack(id));
    }

    @Override
    public String id(ItemStack item) {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        return mythicBukkit.getItemManager().getMythicTypeFromItem(item);
    }
}
