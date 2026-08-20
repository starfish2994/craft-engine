package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.jetbrains.annotations.Nullable;

public record FilterSource(@Nullable String namespace, @Nullable String path) implements SpriteSource {

    static FilterSource fromJson(JsonObject json) {
        String namespace = null;
        String path = null;
        if (json.get("pattern") instanceof JsonObject pattern) {
            if (pattern.has("namespace")) {
                namespace = pattern.get("namespace").getAsString();
            }
            if (pattern.has("path")) {
                path = pattern.get("path").getAsString();
            }
        }
        return new FilterSource(namespace, path);
    }

    static FilterSource fromConfig(ConfigSection config) {
        ConfigSection pattern = config.getSection("pattern");
        if (pattern == null) return new FilterSource(null, null);
        return new FilterSource(pattern.getString("namespace"), pattern.getString("path"));
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "filter");
        JsonObject pattern = new JsonObject();
        if (this.namespace != null) {
            pattern.addProperty("namespace", this.namespace);
        }
        if (this.path != null) {
            pattern.addProperty("path", this.path);
        }
        json.add("pattern", pattern);
        return json;
    }
}
