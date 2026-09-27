package net.momirealms.craftengine.bukkit.plugin.command.debug;

import com.google.gson.stream.JsonWriter;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.block.BlockManager;
import net.momirealms.craftengine.core.block.BlockRegistryMirror;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.sender.Sender;
import net.momirealms.craftengine.core.plugin.network.NetworkManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DebugExportBlockStateMappingsCommand extends BukkitCommandFeature<CommandSender> {

    public DebugExportBlockStateMappingsCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .handler(context -> {
                    Sender sender = plugin().senderFactory().wrap(context.sender());
                    int vanillaStates = plugin().blockManager().vanillaBlockStateCount();
                    int totalStates = BlockRegistryMirror.size();
                    int exportedVanillaStates = 0;
                    int exportedCustomStates = 0;
                    NetworkManager networkManager = plugin().networkManager();
                    Path output = plugin().dataFolderPath().resolve("debug").resolve("block-state-mappings.json");
                    try {
                        Files.createDirectories(output.getParent());
                        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(output))) {
                            writer.setIndent("  ");
                            writer.beginObject();
                            for (int stateId = 0; stateId < totalStates; stateId++) {
                                // 只导出实际发给原版客户端的非恒等映射。
                                int targetId = networkManager.remapBlockState(stateId, false);
                                if (targetId == stateId) continue;
                                String source = stateId < vanillaStates
                                        ? BlockRegistryMirror.byId(stateId).getAsString()
                                        : BlockManager.createCustomBlockKey(stateId - vanillaStates).asString();
                                writer.name(source).value(BlockRegistryMirror.byId(targetId).getAsString());
                                if (stateId < vanillaStates) {
                                    exportedVanillaStates++;
                                } else {
                                    exportedCustomStates++;
                                }
                            }
                            writer.endObject();
                        }
                    } catch (IOException e) {
                        plugin().logger().warn("Failed to export block state mappings", e);
                        sender.sendMessage(DebugCommandOutput.error("Failed to export block state mappings"));
                        sender.sendMessage(DebugCommandOutput.value("Output", output));
                        return;
                    }
                    sender.sendMessage(DebugCommandOutput.success("Exported block state mappings"));
                    sender.sendMessage(DebugCommandOutput.value("Vanilla States", exportedVanillaStates));
                    sender.sendMessage(DebugCommandOutput.value("Custom States", exportedCustomStates));
                    sender.sendMessage(DebugCommandOutput.value("Output", output));
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_export_block_state_mappings";
    }
}
