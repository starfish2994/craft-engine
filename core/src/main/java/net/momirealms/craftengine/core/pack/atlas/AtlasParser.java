package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class AtlasParser {

    private AtlasParser() {
    }

    public static AtlasData parse(JsonObject atlasJson) {
        List<SpriteSource> sources = new ArrayList<>();
        JsonArray sourcesJson = atlasJson.getAsJsonArray("sources");
        if (sourcesJson != null) {
            for (JsonElement element : sourcesJson) {
                if (!(element instanceof JsonObject sourceJson)) continue;
                SpriteSource source = SpriteSource.fromJson(sourceJson);
                if (source != null) {
                    sources.add(source);
                }
            }
        }
        return new AtlasData(sources);
    }

    public static AtlasData parse(@NotNull List<JsonObject> atlasJsons) {
        List<SpriteSource> sources = new ArrayList<>();
        for (JsonObject atlasJson : atlasJsons) {
            if (atlasJson != null) {
                sources.addAll(parse(atlasJson).sources());
            }
        }
        return new AtlasData(sources);
    }

    public static JsonObject toJson(AtlasData atlas) {
        return atlas.get();
    }
}
