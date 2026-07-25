package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record SingleSource(Key resource, @Nullable Key sprite) implements SpriteSource {

    static @Nullable SingleSource fromJson(JsonObject json) {
        if (!json.has("resource")) return null;
        Key resource = Key.of(json.get("resource").getAsString());
        Key sprite = json.has("sprite") ? Key.of(json.get("sprite").getAsString()) : null;
        return new SingleSource(resource, sprite);
    }

    static SingleSource fromConfig(ConfigSection config) {
        return new SingleSource(config.getNonNullKey("resource"), config.getKey("sprite", (Key) null));
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "single");
        json.addProperty("resource", this.resource.toString());
        if (this.sprite != null) {
            json.addProperty("sprite", this.sprite.toString());
        }
        return json;
    }
}
