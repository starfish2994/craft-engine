package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.Tristate;
import net.momirealms.craftengine.core.world.WorldSettings;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.momirealms.craftengine.core.world.WorldSettings.*;

public final class EditWorldSettingsCommand extends BukkitCommandFeature<CommandSender> {
    private static final List<Suggestion> SUGGESTIONS = List.of(
            Suggestion.suggestion(RESTORE_CUSTOM_BLOCK),
            Suggestion.suggestion(RESTORE_VANILLA_BLOCK),
            Suggestion.suggestion(SYNC_CUSTOM_BLOCK)
    );

    public EditWorldSettingsCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("world", WorldParser.worldParser())
                .required("setting", StringParser.stringComponent().suggestionProvider((context, input) -> CompletableFuture.completedFuture(SUGGESTIONS)))
                .required("value", EnumParser.enumComponent(Tristate.class))
                .handler(context -> {
                    World world = context.get("world");
                    String setting = context.get("setting");
                    Tristate value = context.get("value");
                    WorldSettings settings = BukkitAdaptor.adapt(world).storageWorld().settings;
                    settings.set(setting, value);
                });
    }

    @Override
    public String getFeatureID() {
        return "edit_world_settings";
    }
}
