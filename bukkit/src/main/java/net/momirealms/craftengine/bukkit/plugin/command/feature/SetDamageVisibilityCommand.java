package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.attribute.damage.DamageVisibility;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.parser.standard.EnumParser;

import java.util.Locale;

public final class SetDamageVisibilityCommand extends BukkitCommandFeature<CommandSender> {

    public SetDamageVisibilityCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(FlagKeys.SILENT_FLAG)
                .required("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                .required("state", EnumParser.enumParser(DamageVisibility.class))
                .handler(context -> {
                    SinglePlayerSelector selector = context.get("player");
                    BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(selector.single());
                    if (serverPlayer == null) return;
                    DamageVisibility state = context.get("state");
                    serverPlayer.setDamageVisibility(state);
                    handleFeedback(context, MessageConstants.COMMAND_SET_DAMAGE_VISIBILITY_SUCCESS,
                            Component.text(state.name().toLowerCase(Locale.ROOT)), Component.text(serverPlayer.name()));
                });
    }

    @Override
    public String getFeatureID() {
        return "set_damage_visibility";
    }
}
