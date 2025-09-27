package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

public class DebugRealStateUsageCommand extends BukkitCommandFeature<CommandSender> {

    public DebugRealStateUsageCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
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
//                    List<Integer> reals = blockManager.realBlockArranger().get(baseBlockId);
//                    if (reals == null) return;
//                    int i = 0;
//                    Component block = Component.text(baseBlockId + ": ");
//                    plugin().senderFactory().wrap(context.sender()).sendMessage(block);
//
//                    List<Component> batch = new ArrayList<>(100);
//                    for (int real : reals) {
//                        ImmutableBlockState state = blockManager.getImmutableBlockStateUnsafe(real);
//                        if (state.isEmpty()) {
//                            Component hover = Component.text("craftengine:" + baseBlockId.value() + "_" + i).color(NamedTextColor.GREEN);
//                            batch.add(Component.text("|").color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(hover)));
//                        } else {
//                            Component hover = Component.text("craftengine:" + baseBlockId.value() + "_" + i).color(NamedTextColor.RED);
//                            hover = hover.append(Component.newline()).append(Component.text(state.toString()).color(NamedTextColor.GRAY));
//                            batch.add(Component.text("|").color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(hover)));
//                        }
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
        return "debug_real_state_usage";
    }
}
