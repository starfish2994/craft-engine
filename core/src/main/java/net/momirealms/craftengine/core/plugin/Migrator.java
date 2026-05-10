package net.momirealms.craftengine.core.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Migrator {
    private Migrator() {}

    public static void run(CraftEngine plugin) throws IOException {
        Path cacheFolder = plugin.dataFolderPath().resolve("cache");
        if (Files.exists(cacheFolder)) {
            Path customBlockState = cacheFolder.resolve("custom-block-states.json");
            if (Files.exists(customBlockState)) {
                Files.move(customBlockState, cacheFolder.resolve("custom_block_states.json"), StandardCopyOption.REPLACE_EXISTING);
            }
            Path visualBlockState = cacheFolder.resolve("visual-block-states.json");
            if (Files.exists(visualBlockState)) {
                Files.move(visualBlockState, cacheFolder.resolve("visual_block_states.json"), StandardCopyOption.REPLACE_EXISTING);
            }
            Path customModelDataFolder = cacheFolder.resolve("custom-model-data");
            if (Files.exists(customModelDataFolder)) {
                Files.move(customModelDataFolder, cacheFolder.resolve("custom_model_data"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
