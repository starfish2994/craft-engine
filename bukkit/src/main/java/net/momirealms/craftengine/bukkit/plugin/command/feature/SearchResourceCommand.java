package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.config.IdConfigParser;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
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
import java.util.Map;
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
                        return CompletableFuture.completedFuture(BuiltInRegistries.CONFIG_PARSER.entrySet().stream()
                                .map(Map.Entry::getValue)
                                .filter(v -> v instanceof IdConfigParser parser && parser.supportSearch())
                                .map(parserType -> Suggestion.suggestion(parserType.type().asString()))
                                .toList());
                    }
                }))
                .required("resource", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        NamespacedKey parserKey = context.get("type");
                        Collection<Key> keys = BuiltInRegistries.CONFIG_PARSER.get(KeyUtils.namespacedKeyToKey(parserKey))
                                .map(Holder.Reference::value)
                                .filter(it -> it instanceof IdConfigParser parser && parser.supportSearch())
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
                    IdConfigParser parser = (IdConfigParser) BuiltInRegistries.CONFIG_PARSER.get(KeyUtils.namespacedKeyToKey(parserKey))
                            .map(Holder.Reference::value)
                            .filter(it -> it instanceof IdConfigParser p && p.supportSearch())
                            .orElse(null);
                    // Parser 不存在
                    if (parser == null) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_PARSER_NOT_FOUND, Component.text(parserKey.toString()));
                        return;
                    }
                    // Resource 不存在
                    NamespacedKey resourceKey = context.get("resource");
                    Path path = parser.pathById(KeyUtils.namespacedKeyToKey(resourceKey));
                    if (path == null) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_RESOURCE_NOT_FOUND, Component.text(resourceKey.toString()));
                        return;
                    }

                    String shortenPath = shortenPath(path.normalize().toString());
                    String absolutePath = path.normalize().toAbsolutePath().toString();
                    Component clickablePath = Component.text(shortenPath)
                            .color(NamedTextColor.GOLD)
                            .hoverEvent(HoverEvent.showText(Component.translatable("chat.copy.click", NamedTextColor.WHITE)))
                            .clickEvent(ClickEvent.copyToClipboard(absolutePath));
                    handleFeedback(context, MessageConstants.COMMAND_RESOURCE_SEARCH_SUCCESS, Component.text(resourceKey.toString()), clickablePath);
                });
    }

    @Override
    public String getFeatureID() {
        return "search_resource";
    }

    /**
     * 截取路径：从第一个 "/resources" 或 "\resources" 之后开始保留。
     */
    private static String shortenPath(String fullPath) {
        int len = fullPath.length();
        for (int i = 0; i < len; i++) {
            char c = fullPath.charAt(i);
            if (c == '/' || c == '\\') {
                // 分隔符之后紧跟着 "resources" (忽略大小写)
                if (i + 8 < len && fullPath.regionMatches(true, i + 1, "resources", 0, 8)) {
                    return fullPath.substring(i + 1);
                }
            }
        }
        // 无前置路径，直接以 "resources" 开头
        if (fullPath.regionMatches(true, 0, "resources", 0, 8)) {
            return fullPath;
        }
        // 未找到，返回原路径
        return fullPath;
    }
}
