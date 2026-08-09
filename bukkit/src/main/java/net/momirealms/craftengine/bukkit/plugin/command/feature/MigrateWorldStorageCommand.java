package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.world.BukkitStorageAdaptor;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.bukkit.world.chunk.storage.PersistentDataContainerStorage;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.storage.*;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class MigrateWorldStorageCommand extends BukkitCommandFeature<CommandSender> {
    private static final int CHUNKS_PER_REGION = 32;
    private static final int PROGRESS_INTERVAL = 100;
    private static volatile boolean migrationInProgress = false;

    public MigrateWorldStorageCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    private static String regionFilePrefix(StorageType sourceType) {
        return switch (sourceType) {
            case MCA -> MCARegionFileStorage.REGION_FILE_PREFIX;
            case LINEAR -> LinearRegionFileStorage.REGION_FILE_PREFIX;
            case PDC -> MCARegionFileStorage.REGION_FILE_PREFIX; // PDC 数据跟随 vanilla 区块，位于原版 region 目录
            default -> throw new IllegalArgumentException("Unsupported source storage type: " + sourceType);
        };
    }

    private static String regionFileSuffix(StorageType sourceType) {
        return switch (sourceType) {
            case MCA -> MCARegionFileStorage.REGION_FILE_SUFFIX;
            case LINEAR -> LinearRegionFileStorage.REGION_FILE_SUFFIX;
            case PDC -> MCARegionFileStorage.REGION_FILE_SUFFIX;
            default -> throw new IllegalArgumentException("Unsupported source storage type: " + sourceType);
        };
    }

    private static int migrationThreads() {
        return Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder.required("world", WorldParser.worldParser())
                .required("direction", EnumParser.enumComponent(Direction.class))
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

                    if (migrationInProgress) {
                        context.sender().sendMessage("Another migration is already in progress. Please wait until it finishes.");
                        return;
                    }

                    migrationInProgress = true;

                    CommandSender sender = context.sender();
                    World world = context.get("world");
                    Direction direction = context.get("direction");
                    StorageType specifiedType = context.get("type");
                    StorageType configType = Config.chunkStorageType();

                    if (specifiedType != StorageType.MCA && specifiedType != StorageType.PDC && specifiedType != StorageType.LINEAR) {
                        sender.sendMessage("You can only migrate worlds from/to MCA, PDC or LINEAR.");
                        migrationInProgress = false;
                        return;
                    }

                    if (specifiedType == configType) {
                        sender.sendMessage("Specified storage type is the same as current storage type: " + specifiedType + ".");
                        migrationInProgress = false;
                        return;
                    }

                    // from: 从指定类型迁移到当前配置的存储；to: 从当前配置的存储迁移到指定类型
                    StorageType sourceType = direction == Direction.FROM ? specifiedType : configType;
                    StorageType targetType = direction == Direction.FROM ? configType : specifiedType;

                    Path regionFolder = getRegionFolder(world, sourceType);
                    if (!Files.isDirectory(regionFolder)) {
                        sender.sendMessage("Cannot find region folder: " + regionFolder);
                        migrationInProgress = false;
                        return;
                    }

                    List<ChunkPos> regions = getRegionPositions(regionFolder, sourceType);
                    if (regions.isEmpty()) {
                        sender.sendMessage("Cannot find any region file in: " + regionFolder);
                        migrationInProgress = false;
                        return;
                    }

                    BukkitWorld adaptedWorld = BukkitAdaptor.adapt(world);
                    WorldDataStorage liveStorage = adaptedWorld.storageWorld().worldDataStorage();
                    // 当前配置类型的一侧使用世界的 live 存储（脏数据/在途写入可见），
                    // 另一侧新建裸存储，迁移结束时由 finishMigration 关闭以保证落盘
                    WorldDataStorage sourceStorage = direction == Direction.FROM
                            ? createStorage(sourceType, world, adaptedWorld) : liveStorage;
                    WorldDataStorage targetStorage = direction == Direction.FROM
                            ? liveStorage : createStorage(targetType, world, adaptedWorld);
                    WorldDataStorage createdStorage = direction == Direction.FROM ? sourceStorage : targetStorage;

                    MigrationStats stats = new MigrationStats(regions.size() * CHUNKS_PER_REGION * CHUNKS_PER_REGION);

                    ExecutorService executor = newMigrationExecutor(world.getName());

                    sender.sendMessage("Starting storage migration for world: " + world.getName() +
                            ". Source: " + sourceType + ", target: " + targetType +
                            ", regions: " + regions.size() +
                            ", chunks to check: " + stats.total);

                    migrateChunks(sender, regions, world, targetStorage, sourceStorage, stats, executor, createdStorage, direction == Direction.TO ? targetType : null);
                });
    }

    private Path getRegionFolder(World world, StorageType sourceType) {
        return switch (sourceType) {
            case MCA, LINEAR -> world.getWorldFolder().toPath().resolve("craftengine");
            case PDC -> world.getWorldFolder().toPath().resolve("region");
            default -> throw new IllegalArgumentException("Unsupported source storage type: " + sourceType);
        };
    }

    private WorldDataStorage createStorage(StorageType type, World world, BukkitWorld adaptedWorld) {
        ChunkFactory chunkFactory = VersionHelper.hasFoliaPatch ? BukkitStorageAdaptor.FOLIA_FACTORY : BukkitStorageAdaptor.BUKKIT_FACTORY;
        return switch (type) {
            case MCA -> new MCARegionFileStorage(world.getWorldFolder().toPath().resolve("craftengine"), chunkFactory);
            case LINEAR ->
                    new LinearRegionFileStorage(world.getWorldFolder().toPath().resolve("craftengine"), chunkFactory);
            case PDC -> new PersistentDataContainerStorage(adaptedWorld, chunkFactory);
            default -> throw new IllegalArgumentException("Unsupported storage type: " + type);
        };
    }

    private ExecutorService newMigrationExecutor(String worldName) {
        AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "craftengine-storage-migration-" + worldName + "-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(migrationThreads(), threadFactory);
    }

    private void migrateChunks(CommandSender sender, List<ChunkPos> regions, World world,
                               WorldDataStorage targetStorage, WorldDataStorage sourceStorage,
                               MigrationStats stats, ExecutorService executor,
                               WorldDataStorage createdStorage, @Nullable StorageType restartHintType) {
        // PDC 数据存在 vanilla 区块上，涉及 PDC 时必须先拿到区块本体；
        // MCA/LINEAR 的源存储可以直接回答区块是否存在，无需把区块加载进服务器
        boolean requiresWorldChunk = sourceStorage instanceof PersistentDataContainerStorage
                || targetStorage instanceof PersistentDataContainerStorage;
        Semaphore inFlight = new Semaphore(migrationThreads() * 8);
        Thread producer = new Thread(() -> {
            try {
                for (ChunkPos region : regions) {
                    int baseX = region.x();
                    int baseZ = region.z();
                    for (int x = 0; x < CHUNKS_PER_REGION; x++) {
                        for (int z = 0; z < CHUNKS_PER_REGION; z++) {
                            ChunkPos pos = new ChunkPos(baseX + x, baseZ + z);
                            inFlight.acquire();
                            executor.execute(() -> {
                                try {
                                    migrateOne(pos, world, targetStorage, sourceStorage, requiresWorldChunk, stats);
                                } finally {
                                    inFlight.release();
                                }
                            });
                        }
                    }
                }
                executor.shutdown();
                //noinspection ResultOfMethodCallIgnored
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishMigration(sender, world, createdStorage, stats, executor, restartHintType);
            }
        }, "craftengine-storage-migration-producer-" + world.getName());
        producer.setDaemon(true);
        producer.start();
    }

    private void migrateOne(ChunkPos pos, World world, WorldDataStorage targetStorage, WorldDataStorage sourceStorage,
                            boolean requiresWorldChunk, MigrationStats stats) {
        try {
            if (requiresWorldChunk && world.getChunkAtAsync(pos.x(), pos.z(), false).join() == null) {
                stats.skipped.incrementAndGet();
                return;
            }
            CompoundTag tag = sourceStorage.readChunkTagAt(pos);
            if (tag == null) {
                stats.empty.incrementAndGet();
                return;
            }

            targetStorage.writeChunkTagAt(pos, tag);
            stats.migrated.incrementAndGet();
        } catch (Exception e) {
            stats.failed.incrementAndGet();
            this.plugin().logger().warn("Failed to migrate data for chunk at [" + pos.x() + ", " + pos.z() + "]", e);
        } finally {
            int checked = stats.checked.incrementAndGet();
            if (checked % PROGRESS_INTERVAL == 0) {
                this.plugin().logger().info("Storage migration progress for world: " + world.getName() +
                        ". Checked: " + checked + "/" + stats.total +
                        ", migrated: " + stats.migrated.get() +
                        ", empty: " + stats.empty.get() +
                        ", skipped: " + stats.skipped.get() +
                        ", failed: " + stats.failed.get());
            }
        }
    }

    private void finishMigration(CommandSender sender, World world, WorldDataStorage createdStorage, MigrationStats stats,
                                 ExecutorService executor, @Nullable StorageType restartHintType) {
        try {
            createdStorage.close();
        } catch (Exception e) {
            this.plugin().logger().warn("Failed to safely close storage.", e);
        } finally {
            executor.shutdown();
            migrationInProgress = false; // 重置标志
        }

        String message = "Storage migration finished for world: " + world.getName() +
                ". Checked: " + stats.checked.get() + "/" + stats.total +
                ", migrated: " + stats.migrated.get() +
                ", empty: " + stats.empty.get() +
                ", skipped: " + stats.skipped.get() +
                ", failed: " + stats.failed.get();

        this.plugin().logger().info(message);
        sender.sendMessage(message);
        if (restartHintType != null) {
            sender.sendMessage("Now switch chunk-system.storage-type to '" + restartHintType.name().toLowerCase(Locale.ROOT) +
                    "' in the configuration, then restart the server.");
        }
    }

    @Override
    public String getFeatureID() {
        return "migrate_world_storage";
    }

    private List<ChunkPos> getRegionPositions(Path regionFolder, StorageType sourceType) {
        List<ChunkPos> positions = new ArrayList<>(128);

        if (!Files.isDirectory(regionFolder)) return positions;

        String prefix = regionFilePrefix(sourceType);
        String suffix = regionFileSuffix(sourceType);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(regionFolder)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;

                String fileName = entry.getFileName().toString();
                // skip region files of other storage types sharing the folder, swap files and temp files
                if (!fileName.startsWith(prefix) || !fileName.endsWith(suffix)) continue;

                ChunkPos pos = RegionStorage.parseRegionFileCoordinates(entry, prefix, suffix);
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

    public enum Direction {
        FROM,
        TO
    }

    private static final class MigrationStats {
        private final int total;
        private final AtomicInteger checked = new AtomicInteger();
        private final AtomicInteger migrated = new AtomicInteger();
        private final AtomicInteger empty = new AtomicInteger();
        private final AtomicInteger skipped = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();

        private MigrationStats(int total) {
            this.total = total;
        }
    }
}
