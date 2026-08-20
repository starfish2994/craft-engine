package net.momirealms.craftengine.core.pack.model.legacy;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.model.bbmodel.BBModelConverter;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.model.generation.ModelGenerationHolder;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class LegacyItemModel {
    private final Key path;
    private final ModelGeneration generation;
    private final List<LegacyOverridesModel> overrides;

    public LegacyItemModel(Key path, List<LegacyOverridesModel> overrides, ModelGeneration generation) {
        this.generation = generation;
        this.path = path;
        this.overrides = overrides;
    }

    public void prepareModelGeneration(Consumer<ModelGenerationHolder> consumer) {
        if (this.generation != null) {
            consumer.accept(new ModelGenerationHolder(this.path, this.generation));
        }
        for (LegacyOverridesModel override : this.overrides) {
            override.prepareModelGeneration(consumer);
        }
    }

    public List<LegacyOverridesModel> overrides() {
        return this.overrides;
    }

    public Key path() {
        return this.path;
    }

    private static final String[] PATH = ConfigKeys.of("path|model");

    public static LegacyItemModel fromConfig(Pack pack, Path path, ConfigSection section, int customModelData) {
        Pair<Key, ModelGeneration> baseModel = parseModelPath(pack, path, section);
        Key legacyModelPath = baseModel.left();
        ModelGeneration baseModelGeneration = baseModel.right();
        ConfigValue overridesValue = section.getValue("overrides");
        if (overridesValue != null) {
            List<LegacyOverridesModel> legacyOverridesModels = new ArrayList<>();
            legacyOverridesModels.add(new LegacyOverridesModel(null, legacyModelPath, customModelData, baseModelGeneration));
            overridesValue.forEach(v -> {
                ConfigSection overrideSection = v.getAsSection();
                Pair<Key, ModelGeneration> overrideModel = parseModelPath(pack, path, overrideSection);
                ConfigSection predicateSection = overrideSection.getNonNullSection("predicate");
                legacyOverridesModels.add(new LegacyOverridesModel(predicateSection.values(), overrideModel.left(), customModelData, overrideModel.right()));
            });
            return new LegacyItemModel(
                    legacyModelPath,
                    legacyOverridesModels,
                    baseModelGeneration
            );
        } else {
            return new LegacyItemModel(
                    legacyModelPath,
                    List.of(new LegacyOverridesModel(null, legacyModelPath, customModelData, baseModelGeneration)),
                    baseModelGeneration
            );
        }
    }

    private static Pair<Key, ModelGeneration> parseModelPath(Pack pack, Path path, ConfigSection section) {
        ConfigValue blueprintValue = section.getValue("blueprint");
        if (blueprintValue != null) {
            BBModelConverter.Converted converted = BBModelConverter.convert(pack, path, "item", section.getValue(PATH), blueprintValue);
            return Pair.of(converted.model(), ModelGeneration.raw(converted.json(), converted.textures()));
        }
        ConfigValue pathValue = section.getNonNullValue(PATH, ConfigConstants.ARGUMENT_IDENTIFIER);
        ConfigSection generationSection = section.getSection("generation");
        return Pair.of(pathValue.getAsAssetPath(), generationSection != null ? ModelGeneration.of(generationSection) : null);
    }
}
