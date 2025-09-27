package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

public class DebugAppearanceStateUsageCommand extends BukkitCommandFeature<CommandSender> {

    public DebugAppearanceStateUsageCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
//                .required("id", StringParser.stringComponent(StringParser.StringMode.GREEDY_FLAG_YIELDING).suggestionProvider(new SuggestionProvider<>() {
//                    @Override
//                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
//                        return CompletableFuture.completedFuture(plugin().blockManager().blockAppearanceArranger().keySet().stream().map(it -> Suggestion.suggestion(it.toString())).toList());
//                    }
//                }))
                .handler(context -> {
//                    String data = context.get("id");
//                    BukkitBlockManager blockManager = plugin().blockManager();
//                    Key baseBlockId = Key.of(data);
//                    List<Integer> appearances = blockManager.blockAppearanceArranger().get(baseBlockId);
//                    if (appearances == null) return;
//                    int i = 0;
//                    Component block = Component.text(baseBlockId + ": ");
//                    plugin().senderFactory().wrap(context.sender()).sendMessage(block);
//
//                    List<Component> batch = new ArrayList<>();
//                    for (int appearance : appearances) {
//                        Component text = Component.text("|");
//                        List<Integer> reals = blockManager.appearanceToRealStates(appearance);
//                        if (reals == null || reals.isEmpty()) {
//                            Component hover = Component.text(baseBlockId.value() + ":" + i).color(NamedTextColor.GREEN);
//                            hover = hover.append(Component.newline()).append(Component.text(BlockStateUtils.fromBlockData(BlockStateUtils.idToBlockState(appearance)).getAsString()).color(NamedTextColor.GREEN));
//                            text = text.color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(hover));
//                        } else {
//                            Component hover = Component.text(baseBlockId.value() + ":" + i).color(NamedTextColor.RED);
//                            List<Component> hoverChildren = new ArrayList<>();
//                            hoverChildren.add(Component.newline());
//                            hoverChildren.add(Component.text(BlockStateUtils.fromBlockData(BlockStateUtils.idToBlockState(appearance)).getAsString()).color(NamedTextColor.RED));
//                            for (int real : reals) {
//                                hoverChildren.add(Component.newline());
//                                hoverChildren.add(Component.text(blockManager.getImmutableBlockStateUnsafe(real).toString()).color(NamedTextColor.GRAY));
//                            }
//                            text = text.color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(hover.children(hoverChildren)));
//                        }
//                        batch.add(text);
//                        i++;
//                        if (batch.size() == 100) {
//                            plugin().senderFactory().wrap(context.sender())
//                                    .sendMessage(Component.text("").children(batch));
//                            batch.clear();
//                        }
//                    }
//                    if (!batch.isEmpty()) {
//                        plugin().senderFactory().wrap(context.sender())
//                                .sendMessage(Component.text("").children(batch));
//                        batch.clear();
//                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_appearance_state_usage";
    }
}
