package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.jetbrains.annotations.Nullable;

public record DirectorySource(String source, String prefix) implements SpriteSource {

    static @Nullable DirectorySource fromJson(JsonObject json) {
        if (!json.has("source") || !json.has("prefix")) return null;
        return new DirectorySource(json.get("source").getAsString(), json.get("prefix").getAsString());
    }

    static DirectorySource fromConfig(ConfigSection config) {
        return new DirectorySource(config.getNonNullString("source"), config.getNonNullString("prefix"));
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "directory");
        json.addProperty("source", this.source);
        json.addProperty("prefix", this.prefix);
        return json;
    }
}
