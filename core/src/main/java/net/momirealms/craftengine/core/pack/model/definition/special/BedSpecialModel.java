package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.Objects;

public final class BedSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<BedSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<BedSpecialModel> READER = new Reader();
    private final String part;
    private final String texture;

    public BedSpecialModel(String part, String texture) {
        this.part = part;
        this.texture = texture;
    }

    public String part() {
        return this.part;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bed");
        if (version.isAtOrAbove(MinecraftVersion.V26_1)) {
            json.addProperty("part", Objects.requireNonNull(this.part)); // 必填项只能在生成时检查
        }
        json.addProperty("texture", this.texture);
        return json;
    }

    private static class Factory implements SpecialModelFactory<BedSpecialModel> {
        @Override
        public BedSpecialModel create(ConfigSection section) {
            return new BedSpecialModel(
                    section.getString("part"),
                    section.getNonNullIdentifier("texture").asMinimalString()
            );
        }
    }

    private static class Reader implements SpecialModelReader<BedSpecialModel> {
        @Override
        public BedSpecialModel read(JsonObject json) {
            return new BedSpecialModel(
                    json.has("part") ? json.get("part").getAsString() : null,
                    json.get("texture").getAsString()
            );
        }
    }
}
