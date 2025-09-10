package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.util.SetMonitor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class PackCacheData {
    private final Set<Path> externalZips;
    private final Set<Path> externalFolders;

    PackCacheData(@NotNull CraftEngine plugin) {
        this.externalFolders = new SetMonitor<>(
                Config.foldersToMerge().stream()
                        .map(it -> plugin.dataFolderPath().getParent().resolve(it))
                        .filter(Files::exists)
                        .collect(Collectors.toSet()),
                add -> plugin.logger().info("Adding external folder: " + add),
                remove -> plugin.logger().info("Removing external folder: " + remove),
                true
        );
        this.externalZips = new SetMonitor<>(
                Config.zipsToMerge().stream()
                        .map(it -> plugin.dataFolderPath().getParent().resolve(it))
                        .filter(Files::exists)
                        .filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(".zip"))
                        .collect(Collectors.toSet()),
                add -> plugin.logger().info("Adding external zip: " + add),
                remove -> plugin.logger().info("Removing external zip: " + remove),
                true
        );
    }

    @NotNull
    public Set<Path> externalFolders() {
        return this.externalFolders;
    }

    @NotNull
    public Set<Path> externalZips() {
        return this.externalZips;
    }
}
