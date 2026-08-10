package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.IdConfigParser;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SearchResourceCommand extends BukkitCommandFeature<CommandSender> {

    public SearchResourceCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("type", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(BuiltInRegistries.CONFIG_PARSER.keySet().stream()
                                .map(parserType -> Suggestion.suggestion(parserType.asString()))
                                .toList());
                    }
                }))
                .required("resource", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        NamespacedKey parserKey = context.get("type");
                        Collection<Key> keys = BuiltInRegistries.CONFIG_PARSER.get(Key.from(parserKey.asString()))
                                .map(Holder.Reference::value)
                                .filter(it -> it instanceof IdConfigParser)
                                .map(it -> ((IdConfigParser) it).registeredKeys())
                                .orElse(null);
                        if (keys == null || keys.isEmpty()) {
                            return CompletableFuture.completedFuture(List.of());
                        } else {
                            return CompletableFuture.completedFuture(keys.stream()
                                    .map(parserType -> Suggestion.suggestion(parserType.asString()))
                                    .toList());
                        }
                    }
                }))
                .handler(context -> {
                    NamespacedKey parserKey = context.get("type");
                    IdConfigParser parser = (IdConfigParser) BuiltInRegistries.CONFIG_PARSER.get(Key.from(parserKey.asString()))
                            .map(Holder.Reference::value)
                            .filter(it -> it instanceof IdConfigParser)
                            .orElse(null);
                    // Parser 不存在
                    if (parser == null) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_PARSER_NOT_FOUND, Component.text(parserKey.asString()));
                        return;
                    }
                    // Resource 不存在
                    NamespacedKey resourceKey = context.get("resource");
                    Path path = parser.pathById(Key.from(resourceKey.asString()));
                    if (path == null) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_RESOURCE_NOT_FOUND, Component.text(resourceKey.asString()));
                        return;
                    }

                    String pathStr = path.normalize().toString();
                    handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_RESOURCE_SUCCESS, Component.text(resourceKey.asString()), Component.text(pathStr));
                });
    }

    @Override
    public String getFeatureID() {
        return "search_resource";
    }
}
