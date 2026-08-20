package net.momirealms.craftengine.bukkit.compatibility.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.script.ScriptManager;
import net.momirealms.craftengine.core.plugin.script.placeholder.ScriptPlaceholderManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScriptPlaceholderExpansion extends PlaceholderExpansion implements Relational {
    private final CraftEngine plugin;

    public ScriptPlaceholderExpansion(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cejs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        ScriptPlaceholderManager placeholderManager = placeholderManager();
        if (placeholderManager == null) return null;
        return placeholderManager.resolve(params, player);
    }

    @Nullable
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        ScriptPlaceholderManager placeholderManager = placeholderManager();
        if (placeholderManager == null) return null;
        return placeholderManager.resolve(params, player);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player one, Player two, @NotNull String params) {
        ScriptPlaceholderManager placeholderManager = placeholderManager();
        if (placeholderManager == null) return null;
        return placeholderManager.resolveRelational(params, one, two);
    }

    @Nullable
    private ScriptPlaceholderManager placeholderManager() {
        ScriptManager scriptManager = this.plugin.scriptManager();
        if (scriptManager == null || !scriptManager.isAvailable()) return null;
        return scriptManager.placeholderManager();
    }
}
