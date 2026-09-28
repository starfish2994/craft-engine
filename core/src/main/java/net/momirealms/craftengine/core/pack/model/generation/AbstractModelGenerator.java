package net.momirealms.craftengine.core.pack.model.generation;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractModelGenerator implements ModelGenerator {
    protected final CraftEngine plugin;
    protected final Map<Key, ModelGeneration> modelsToGenerate = new ConcurrentHashMap<>();
    protected final Map<Key, byte[]> texturesToGenerate = new ConcurrentHashMap<>();
    protected final Map<Key, JsonObject> textureMetadataToGenerate = new ConcurrentHashMap<>();

    public AbstractModelGenerator(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public Map<Key, ModelGeneration> modelsToGenerate() {
        return this.modelsToGenerate;
    }

    @Override
    public Map<Key, byte[]> texturesToGenerate() {
        return this.texturesToGenerate;
    }

    @Override
    public Map<Key, JsonObject> textureMetadataToGenerate() {
        return this.textureMetadataToGenerate;
    }

    @Override
    public void clearModelsToGenerate() {
        this.modelsToGenerate.clear();
        this.texturesToGenerate.clear();
        this.textureMetadataToGenerate.clear();
    }

    public void prepareModelGeneration(ModelGenerationHolder holder) {
        this.modelsToGenerate.compute(holder.path(), (k, conflict) -> {
            if (conflict != null && !conflict.isSameJsonModel(holder.model())) {
                throw new KnownResourceException("resource.model.generation.conflict", holder.path().asString());
            }
            return holder.model();
        });
        holder.model().rawTextures().forEach((path, png) -> prepareTextureGeneration(path, png, holder.model().rawTextureMetadata().get(path)));
    }

    private void prepareTextureGeneration(Key path, byte[] png, JsonObject metadata) {
        this.texturesToGenerate.compute(path, (k, conflict) -> {
            if (conflict != null && (!Arrays.equals(conflict, png) || !Objects.equals(this.textureMetadataToGenerate.get(path), metadata))) {
                throw new KnownResourceException("resource.texture.generation.conflict", path.asString());
            }
            if (metadata != null) this.textureMetadataToGenerate.put(path, metadata);
            return png;
        });
    }
}
