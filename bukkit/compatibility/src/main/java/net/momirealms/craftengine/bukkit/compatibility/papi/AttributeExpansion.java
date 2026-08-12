package net.momirealms.craftengine.bukkit.compatibility.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class AttributeExpansion extends PlaceholderExpansion {
    private final CraftEngine plugin;

    public AttributeExpansion(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "ceattr";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "XiaoMoMi";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player bukkitPlayer, @NotNull String params) {
        if (bukkitPlayer == null) return null;
        String[] split = StringUtils.split(params, '_', 2);
        if (split.length != 2) return null;
        Optional<Attribute> optionalAttribute = this.plugin.attributeManager().getAttribute(Key.of(split[1]));
        if (optionalAttribute.isEmpty()) return null;
        Attribute attribute = optionalAttribute.get();
        BukkitServerPlayer player = BukkitAdaptor.adapt(bukkitPlayer);
        AttributeManager manager = this.plugin.attributeManager();
        return switch (split[0]) {
            case "base" -> String.valueOf(attribute.baseValueSource().resolve(player));
            case "value" -> String.valueOf(manager.getAttributeValue(player, attribute));
            case "formatted" -> attribute.format(manager.getAttributeValue(player, attribute));
            default -> null;
        };
    }
}
