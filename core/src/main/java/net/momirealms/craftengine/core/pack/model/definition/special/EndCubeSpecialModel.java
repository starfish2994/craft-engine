package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;

public final class EndCubeSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<EndCubeSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<EndCubeSpecialModel> READER = new Reader();
    private final String effect;

    public EndCubeSpecialModel(String effect) {
        this.effect = effect;
    }

    public String effect() {
        return this.effect;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "end_cube");
        json.addProperty("effect", this.effect);
        return json;
    }

    private static class Factory implements SpecialModelFactory<EndCubeSpecialModel> {
        @Override
        public EndCubeSpecialModel create(ConfigSection section) {
            return new EndCubeSpecialModel(section.getNonNullString("effect"));
        }
    }

    private static class Reader implements SpecialModelReader<EndCubeSpecialModel> {
        @Override
        public EndCubeSpecialModel read(JsonObject json) {
            return new EndCubeSpecialModel(json.get("effect").getAsString());
        }
    }
}
