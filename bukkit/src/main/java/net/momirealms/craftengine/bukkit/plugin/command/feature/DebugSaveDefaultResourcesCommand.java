package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

public class DebugSaveDefaultResourcesCommand extends BukkitCommandFeature<CommandSender> {

    public DebugSaveDefaultResourcesCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .handler(context -> {
                    AbstractPackManager packManager = (AbstractPackManager) CraftEngine.instance().packManager();
                    packManager.saveDefaultConfigs();
                    context.sender().sendMessage("Saved default configs");
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_save_default_resources";
    }
}
