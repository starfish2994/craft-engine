package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.font.BukkitFontManager;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.pack.allocator.IdAllocator;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DebugCleanCacheCommand extends BukkitCommandFeature<CommandSender> {

    public DebugCleanCacheCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("type", StringParser.stringComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(List.of(Suggestion.suggestion("custom-model-data"), Suggestion.suggestion("custom-block-states"), Suggestion.suggestion("visual-block-states"), Suggestion.suggestion("font")));
                    }
                }))
                .handler(context -> {
                    if (this.plugin().isReloading()) {
                        context.sender().sendMessage("The plugin is reloading. Please wait until the process is complete.");
                        return;
                    }
                    String type = context.get("type");
                    switch (type) {
                        case "custom-model-data" -> {
                            BukkitItemManager instance = BukkitItemManager.instance();
                            Set<String> ids = CraftEngineItems.loadedItems().keySet().stream().map(Key::toString).collect(Collectors.toSet());
                            int total = 0;
                            for (Map.Entry<Key, IdAllocator> entry : instance.itemParser().idAllocators().entrySet()) {
                                List<String> removed = entry.getValue().cleanupUnusedIds(i -> !ids.contains(i));
                                total += removed.size();
                                try {
                                    entry.getValue().saveToCache();
                                } catch (IOException e) {
                                    this.plugin().logger().warn("Error while saving custom model data allocation for material " + entry.getKey().asString(), e);
                                    return;
                                }
                                for (String id : removed) {
                                    this.plugin().logger().info("Cleaned unused item: " + id);
                                }
                            }
                            context.sender().sendMessage("Cleaned " + total + " unused custom model data");
                        }
                        case "custom-block-states" -> {
                        }
                        case "visual-block-states" -> {
                        }
                        case "font", "images" -> {
                            BukkitFontManager instance = this.plugin().fontManager();

                        }
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_clean_cache";
    }
}
