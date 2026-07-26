package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public record AtlasData(List<SpriteSource> sources) implements Supplier<JsonObject> {

    public AtlasData {
        sources = List.copyOf(sources);
    }

    public static AtlasData merge(@NotNull List<AtlasData> atlases) {
        if (atlases.isEmpty()) return new AtlasData(List.of());
        if (atlases.size() == 1) return atlases.getFirst();
        List<SpriteSource> merged = new ArrayList<>();
        for (AtlasData atlas : atlases) {
            if (atlas != null) {
                merged.addAll(atlas.sources());
            }
        }
        return new AtlasData(merged);
    }

    // 去除完全相同的source，合并特征相似的source。
    // 同名贴图在游戏中先定义的生效，因此去重和合并不改变最终图集内容
    public AtlasData optimize() {
        List<SpriteSource> optimized = new ArrayList<>(this.sources.size());
        Set<SpriteSource> seen = new HashSet<>();
        Map<UnstitchKey, Integer> unstitchIndexes = new HashMap<>();
        Map<PermutationKey, Integer> permutationIndexes = new HashMap<>();
        for (SpriteSource source : this.sources) {
            switch (source) {
                case UnstitchSource unstitch -> {
                    UnstitchKey key = new UnstitchKey(unstitch.resource(), unstitch.divisorX(), unstitch.divisorY());
                    Integer index = unstitchIndexes.get(key);
                    if (index == null) {
                        unstitchIndexes.put(key, optimized.size());
                        optimized.add(unstitch);
                    } else {
                        UnstitchSource existing = (UnstitchSource) optimized.get(index);
                        List<UnstitchSource.Region> regions = new ArrayList<>(existing.regions());
                        for (UnstitchSource.Region region : unstitch.regions()) {
                            if (!regions.contains(region)) {
                                regions.add(region);
                            }
                        }
                        optimized.set(index, new UnstitchSource(existing.resource(), existing.divisorX(), existing.divisorY(), regions));
                    }
                }
                case PalettedPermutationsSource permutationsSource -> {
                    PermutationKey key = new PermutationKey(permutationsSource.separator(), permutationsSource.paletteKey());
                    Integer index = permutationIndexes.get(key);
                    if (index == null) {
                        permutationIndexes.put(key, optimized.size());
                        optimized.add(permutationsSource);
                    } else {
                        PalettedPermutationsSource existing = (PalettedPermutationsSource) optimized.get(index);
                        List<Key> textures = new ArrayList<>(existing.textures());
                        for (Key texture : permutationsSource.textures()) {
                            if (!textures.contains(texture)) {
                                textures.add(texture);
                            }
                        }
                        Map<String, Key> permutations = new LinkedHashMap<>(existing.permutations());
                        permutationsSource.permutations().forEach(permutations::putIfAbsent);
                        optimized.set(index, new PalettedPermutationsSource(textures, existing.separator(), existing.paletteKey(), permutations));
                    }
                }
                default -> {
                    if (seen.add(source)) {
                        optimized.add(source);
                    }
                }
            }
        }
        return new AtlasData(optimized);
    }

    private record UnstitchKey(Key resource, double divisorX, double divisorY) {
    }

    private record PermutationKey(String separator, Key paletteKey) {
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        JsonArray sources = new JsonArray();
        for (SpriteSource source : this.sources) {
            sources.add(source.get());
        }
        json.add("sources", sources);
        return json;
    }
}
