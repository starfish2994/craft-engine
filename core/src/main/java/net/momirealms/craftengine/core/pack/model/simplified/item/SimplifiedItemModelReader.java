package net.momirealms.craftengine.core.pack.model.simplified.item;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.model.bbmodel.BBModelConverter;
import net.momirealms.craftengine.core.pack.model.definition.ItemModel;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface SimplifiedItemModelReader {

    ItemModel read(ConfigValue textureValue, Optional<ConfigValue> optionalModelValue, Key id);

    ItemModel read(ConfigValue modelValue);

    int modelCount();

    ItemModel buildFromBlueprints(List<BBModelConverter.Converted> blueprints);

    default ItemModel readBlueprints(ConfigValue blueprintValue, @Nullable ConfigValue modelValue, Pack pack, Path configFile) {
        int count = modelCount();
        List<String> files = blueprintValue.getAsFixedSizeList(count, ConfigValue::getAsString);
        List<Key> models = null;
        if (modelValue != null) {
            models = modelValue.getAsFixedSizeList(count, ConfigValue::getAsAssetPath);
        }
        List<BBModelConverter.Converted> blueprints = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            BBModelConverter.ResolvedBlueprint blueprint = BBModelConverter.resolveBlueprint(pack, configFile, files.get(i));
            blueprints.add(models != null
                    ? BBModelConverter.convert(blueprint.file(), models.get(i), blueprintValue.path())
                    : BBModelConverter.convert(blueprint, pack.namespace(), "item", blueprintValue.path())
            );
        }
        return buildFromBlueprints(blueprints);
    }
}
