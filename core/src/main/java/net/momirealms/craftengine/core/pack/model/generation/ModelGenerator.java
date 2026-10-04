package net.momirealms.craftengine.core.pack.model.generation;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface ModelGenerator {

    Map<Key, ModelGeneration> modelsToGenerate();

    Map<Key, byte[]> texturesToGenerate();

    default Map<Key, JsonObject> textureMetadataToGenerate() {
        return Map.of();
    }

    void clearModelsToGenerate();
}
