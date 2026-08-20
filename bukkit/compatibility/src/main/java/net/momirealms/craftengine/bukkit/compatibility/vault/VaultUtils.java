package net.momirealms.craftengine.bukkit.compatibility.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

public final class VaultUtils {
    @Nullable
    private static Economy economy;

    private VaultUtils() {}

    public static void init() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = provider != null ? provider.getProvider() : null;
    }

    public static boolean hasEconomy() {
        return economy != null;
    }

    public static double balance(OfflinePlayer player) {
        return economy != null ? economy.getBalance(player) : 0;
    }

    public static boolean has(OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        return economy != null && economy.depositPlayer(player, amount).transactionSuccess();
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount) && economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}
