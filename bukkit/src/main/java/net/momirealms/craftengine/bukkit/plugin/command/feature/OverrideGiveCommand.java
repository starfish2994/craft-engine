//package net.momirealms.craftengine.bukkit.plugin.command.feature;
//
//import net.kyori.adventure.text.Component;
//import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
//import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
//import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
//import net.momirealms.craftengine.bukkit.util.PlayerUtils;
//import net.momirealms.craftengine.core.item.Item;
//import net.momirealms.craftengine.core.plugin.CraftEngine;
//import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
//import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
//import net.momirealms.craftengine.core.util.Key;
//import org.bukkit.NamespacedKey;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.incendo.cloud.Command;
//import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
//import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
//import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
//import org.incendo.cloud.context.CommandContext;
//import org.incendo.cloud.context.CommandInput;
//import org.incendo.cloud.parser.standard.IntegerParser;
//import org.incendo.cloud.suggestion.Suggestion;
//import org.incendo.cloud.suggestion.SuggestionProvider;
//
//import java.util.Collection;
//import java.util.concurrent.CompletableFuture;
//
//public class OverrideGiveCommand extends BukkitCommandFeature<CommandSender> {
//
//    public OverrideGiveCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
//        super(commandManager, plugin);
//    }
//
//    @Override
//    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
//        return builder
//                .required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser(true))
//                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
//                    @Override
//                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
//                        return CompletableFuture.completedFuture(plugin().itemManager().cachedAllItemSuggestions());
//                    }
//                }))
//                .optional("amount", IntegerParser.integerParser(1, 6400))
//                .handler(context -> {
//                    MultiplePlayerSelector selector = context.get("player");
//                    int amount = context.getOrDefault("amount", 1);
//                    NamespacedKey namespacedKey = context.get("id");
//                    Key itemId = Key.of(namespacedKey.namespace(), namespacedKey.value());
//                    Collection<Player> players = selector.values();
//
//                    Component anyItemDisplayName = null;
//
//                    for (Player player : players) {
//                        Item<ItemStack> builtItem = BukkitItemManager.instance().createWrappedItem(itemId, BukkitAdaptors.adapt(player));
//                        if (builtItem == null) {
//                            return;
//                        }
//                        int maxStack = builtItem.maxStackSize();
//                        anyItemDisplayName = builtItem.hoverNameComponent()
//                                .orElseGet(() -> {
//                                    if (builtItem.isCustomItem()) {
//                                        return Component.text(itemId.asString());
//                                    } else {
//                                        return Component.translatable("item.minecraft." + itemId.value());
//                                    }
//                                });
//
//                        if (amount > maxStack * 100) {
//                            plugin().senderFactory().wrap(context.sender()).sendMessage(Component.translatable("commands.give.failed.toomanyitems", Component.text(maxStack * 100), anyItemDisplayName);
//                            return;
//                        }
//
//                        PlayerUtils.giveItem(player, amount, builtItem);
//                    }
//                    if (players.size() == 1) {
//                        plugin().senderFactory().wrap(context.sender()).sendMessage(Component.translatable("commands.give.success.single", Component.text(amount), anyItemDisplayName, ));
//                    } else if (players.size() > 1) {
//                        handleFeedback(context, MessageConstants.COMMAND_ITEM_GIVE_SUCCESS_MULTIPLE, Component.text(amount), Component.text(itemId.toString()), Component.text(players.size()));
//                    }
//                });
//    }
//
//    @Override
//    public String getFeatureID() {
//        return "override_minecraft_give";
//    }
//}
