package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.LevelUtils;
import net.momirealms.craftengine.bukkit.world.chunk.storage.PersistentDataContainerStorage;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.storage.*;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.CraftChunkProxy;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerLevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.ChunkAccessProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.LevelChunkProxy;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClearWorldStorageCommand extends BukkitCommandFeature<CommandSender> {
    private static final int CHUNKS_PER_REGION = 32;
    private static final int PROGRESS_INTERVAL = 100;
    private static volatile boolean cleanupInProgress = false;

    public ClearWorldStorageCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder.required("world", WorldParser.worldParser())
                .required("type", EnumParser.enumComponent(StorageType.class)
                        .suggestionProvider(new SuggestionProvider<>() {
                            @Override
                            public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext context, @NonNull CommandInput input) {
                                List<Suggestion> suggestions = new ArrayList<>(2);
                                for (StorageType type : StorageType.values()) {
                                    if (type != Config.chunkStorageType() && type != StorageType.NONE) {
                                        suggestions.add(Suggestion.suggestion(type.name().toLowerCase(Locale.ROOT)));
                                    }
                                }
                                return CompletableFuture.completedFuture(suggestions);
                            }
                        }))
                .handler(context -> {
                    if (!VersionHelper.hasPaperPatch) {
                        context.sender().sendMessage("Spigot is not supported");
                        return;
                    }

                    if (cleanupInProgress) {
                        context.sender().sendMessage("Another cleanup is already in progress. Please wait until it finishes.");
                        return;
                    }

                    CommandSender sender = context.sender();
                    World world = context.get("world");
                    StorageType type = context.get("type");

                    if (type != StorageType.MCA && type != StorageType.PDC && type != StorageType.LINEAR) {
                        sender.sendMessage("You can only clear MCA, PDC or LINEAR storage data.");
                        return;
                    }

                    if (type == Config.chunkStorageType()) {
                        sender.sendMessage("Cannot clear the currently active storage type: " + type + ".");
                        return;
                    }

                    cleanupInProgress = true;
                    if (type == StorageType.PDC) {
                        clearPdcData(world, sender);
                    } else {
                        clearRegionFiles(world, type, sender);
                    }
                });
    }

    private void clearRegionFiles(World world, StorageType type, CommandSender sender) {
        this.plugin().scheduler().async().execute(() -> {
            int deleted = 0;
            int failed = 0;
            try {
                Path folder = world.getWorldFolder().toPath().resolve("craftengine");
                String prefix = type == StorageType.MCA ? MCARegionFileStorage.REGION_FILE_PREFIX : LinearRegionFileStorage.REGION_FILE_PREFIX;
                String suffix = type == StorageType.MCA ? MCARegionFileStorage.REGION_FILE_SUFFIX : LinearRegionFileStorage.REGION_FILE_SUFFIX;
                List<Path> toDelete = new ArrayList<>();
                if (Files.isDirectory(folder)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                        for (Path entry : stream) {
                            if (!Files.isRegularFile(entry)) continue;
                            String fileName = entry.getFileName().toString();
                            boolean isRegionFile = fileName.startsWith(prefix) && fileName.endsWith(suffix);
                            boolean isExternalChunk = type == StorageType.MCA && fileName.startsWith(RegionFile.EXTERNAL_FILE_PREFIX) && fileName.endsWith(RegionFile.EXTERNAL_FILE_SUFFIX);
                            if (isRegionFile || isExternalChunk) {
                                toDelete.add(entry);
                            }
                        }
                    }
                }
                if (toDelete.isEmpty()) {
                    sender.sendMessage("No " + type + " storage files found in: " + folder);
                    return;
                }
                sender.sendMessage("Deleting " + toDelete.size() + " " + type + " storage files for world: " + world.getName());
                for (Path entry : toDelete) {
                    try {
                        Files.deleteIfExists(entry);
                        deleted++;
                    } catch (IOException e) {
                        failed++;
                        this.plugin().logger().warn("Failed to delete file: " + entry, e);
                    }
                }
            } catch (IOException e) {
                this.plugin().logger().warn("Failed to scan region folder for world: " + world.getName(), e);
            } finally {
                cleanupInProgress = false;
            }
            String message = "Storage cleanup finished for world: " + world.getName() +
                    ". Type: " + type + ", deleted: " + deleted + ", failed: " + failed;
            this.plugin().logger().info(message);
            sender.sendMessage(message);
        });
    }

    private void clearPdcData(World world, CommandSender sender) {
        Path regionFolder = world.getWorldFolder().toPath().resolve("region");
        if (!Files.isDirectory(regionFolder)) {
            sender.sendMessage("Cannot find region folder: " + regionFolder);
            cleanupInProgress = false;
            return;
        }

        List<ChunkPos> regions = getRegionPositions(regionFolder);
        if (regions.isEmpty()) {
            sender.sendMessage("Cannot find any region file in: " + regionFolder);
            cleanupInProgress = false;
            return;
        }

        CleanupStats stats = new CleanupStats(regions.size() * CHUNKS_PER_REGION * CHUNKS_PER_REGION);

        sender.sendMessage("Starting PDC storage cleanup for world: " + world.getName() +
                ". Regions: " + regions.size() + ", chunks to check: " + stats.total);

        Executor async = this.plugin().scheduler().async();
        async.execute(() -> {
            for (ChunkPos region : regions) {
                int baseX = region.x();
                int baseZ = region.z();
                CompletableFuture<?>[] batch = new CompletableFuture<?>[CHUNKS_PER_REGION * CHUNKS_PER_REGION];
                int index = 0;
                for (int x = 0; x < CHUNKS_PER_REGION; x++) {
                    for (int z = 0; z < CHUNKS_PER_REGION; z++) {
                        ChunkPos pos = new ChunkPos(baseX + x, baseZ + z);
                        batch[index++] = clearPdcAt(pos, world, stats, async);
                    }
                }
                CompletableFuture.allOf(batch).join();
            }

            cleanupInProgress = false;
            String message = "PDC storage cleanup finished for world: " + world.getName() +
                    ". Checked: " + stats.checked.get() + "/" + stats.total +
                    ", cleared: " + stats.cleared.get() +
                    ", empty: " + stats.empty.get() +
                    ", skipped: " + stats.skipped.get() +
                    ", failed: " + stats.failed.get();
            this.plugin().logger().info(message);
            sender.sendMessage(message);
        });
    }

    // 不在 worker 线程上 join chunk future（ForkJoinPool 的 helpAsyncBlocker 补偿会把等待变成无限递归），
    // 而是把清理逻辑挂到 future 完成后的回调链上
    private CompletableFuture<Void> clearPdcAt(ChunkPos pos, World world, CleanupStats stats, Executor async) {
        return world.getChunkAtAsync(pos.x(), pos.z(), false).thenAcceptAsync(chunk -> {
            try {
                if (chunk == null) {
                    stats.skipped.incrementAndGet();
                    return;
                }
                PersistentDataContainer pdc = chunk.getPersistentDataContainer();
                if (pdc.get(PersistentDataContainerStorage.CHUNK_KEY, PersistentDataType.BYTE_ARRAY) != null) {
                    pdc.remove(PersistentDataContainerStorage.CHUNK_KEY);
                    markChunkUnsaved(chunk);
                    stats.cleared.incrementAndGet();
                } else {
                    stats.empty.incrementAndGet();
                }
            } catch (Exception e) {
                stats.failed.incrementAndGet();
                this.plugin().logger().warn("Failed to clear PDC data for chunk at [" + pos.x() + ", " + pos.z() + "]", e);
            } finally {
                int checked = stats.checked.incrementAndGet();
                if (checked % PROGRESS_INTERVAL == 0) {
                    this.plugin().logger().info("PDC storage cleanup progress for world: " + world.getName() +
                            ". Checked: " + checked + "/" + stats.total +
                            ", cleared: " + stats.cleared.get() +
                            ", empty: " + stats.empty.get() +
                            ", skipped: " + stats.skipped.get() +
                            ", failed: " + stats.failed.get());
                }
            }
        }, async);
    }

    private static void markChunkUnsaved(Chunk chunk) {
        Object worldServer = CraftChunkProxy.INSTANCE.getWorld(chunk);
        Object chunkSource = ServerLevelProxy.INSTANCE.getChunkSource(worldServer);
        Object levelChunk = LevelUtils.getChunkAtIfLoaded(chunkSource, chunk.getX(), chunk.getZ());
        if (levelChunk == null) return;
        if (VersionHelper.isOrAbove1_21_2) {
            LevelChunkProxy.INSTANCE.markUnsaved(levelChunk);
        } else {
            ChunkAccessProxy.INSTANCE.setUnsaved(levelChunk, true);
        }
    }

    private List<ChunkPos> getRegionPositions(Path regionFolder) {
        List<ChunkPos> positions = new ArrayList<>(128);

        if (!Files.isDirectory(regionFolder)) return positions;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(regionFolder)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;

                String fileName = entry.getFileName().toString();
                if (!fileName.startsWith(MCARegionFileStorage.REGION_FILE_PREFIX) || !fileName.endsWith(MCARegionFileStorage.REGION_FILE_SUFFIX))
                    continue;

                ChunkPos pos = RegionStorage.parseRegionFileCoordinates(entry, MCARegionFileStorage.REGION_FILE_PREFIX, MCARegionFileStorage.REGION_FILE_SUFFIX);
                if (pos == null) {
                    this.plugin().logger().warn("Invalid region file name: " + fileName);
                    continue;
                }
                positions.add(pos);
            }
        } catch (IOException e) {
            this.plugin().logger().warn("Failed to read region folder: " + regionFolder, e);
        }

        positions.sort(Comparator.comparingInt(ChunkPos::x).thenComparingInt(ChunkPos::z));
        return positions;
    }

    @Override
    public String getFeatureID() {
        return "clear_world_storage";
    }

    private static final class CleanupStats {
        private final int total;
        private final AtomicInteger checked = new AtomicInteger();
        private final AtomicInteger cleared = new AtomicInteger();
        private final AtomicInteger empty = new AtomicInteger();
        private final AtomicInteger skipped = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();

        private CleanupStats(int total) {
            this.total = total;
        }
    }
}
