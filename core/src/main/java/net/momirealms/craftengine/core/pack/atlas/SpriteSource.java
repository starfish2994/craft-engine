package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public sealed interface SpriteSource extends Supplier<JsonObject>
        permits DirectorySource, SingleSource, FilterSource, UnstitchSource, PalettedPermutationsSource {

    static @Nullable SpriteSource fromJson(JsonObject json) {
        if (!json.has("type")) return null;
        String type = json.get("type").getAsString();
        return switch (type) {
            case "directory", "minecraft:directory" -> DirectorySource.fromJson(json);
            case "single", "minecraft:single" -> SingleSource.fromJson(json);
            case "filter", "minecraft:filter" -> FilterSource.fromJson(json);
            case "unstitch", "minecraft:unstitch" -> UnstitchSource.fromJson(json);
            case "paletted_permutations", "minecraft:paletted_permutations" -> PalettedPermutationsSource.fromJson(json);
            default -> null;
        };
    }

    static SpriteSource fromConfig(ConfigSection config) {
        String type = config.getNonNullString("type");
        return switch (type) {
            case "directory", "minecraft:directory" -> DirectorySource.fromConfig(config);
            case "single", "minecraft:single" -> SingleSource.fromConfig(config);
            case "filter", "minecraft:filter" -> FilterSource.fromConfig(config);
            case "unstitch", "minecraft:unstitch" -> UnstitchSource.fromConfig(config);
            case "paletted_permutations", "minecraft:paletted_permutations" -> PalettedPermutationsSource.fromConfig(config);
            default -> throw new IllegalArgumentException("Unknown atlas source type: " + type);
        };
    }
}
