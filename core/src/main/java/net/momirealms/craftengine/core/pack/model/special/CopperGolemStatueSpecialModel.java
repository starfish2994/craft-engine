package net.momirealms.craftengine.core.pack.model.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public class CopperGolemStatueSpecialModel implements SpecialModel {
    public static final Factory FACTORY = new Factory();
    public static final Reader READER = new Reader();
    private final String pose;
    private final String texture;

    public CopperGolemStatueSpecialModel(String pose, String texture) {
        this.pose = pose;
        this.texture = texture;
    }

    @Override
    public Key type() {
        return SpecialModels.COPPER_GOLEM_STATUE;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.addProperty("pose", this.pose);
        json.addProperty("texture", this.texture);
        return json;
    }

    public static class Factory implements SpecialModelFactory {
        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            String pose = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("pose"), "warning.config.item.model.special.copper_golem_statue.missing_pose");
            String texture = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("texture"), "warning.config.item.model.special.copper_golem_statue.missing_texture");
            return new CopperGolemStatueSpecialModel(pose, texture);
        }
    }

    public static class Reader implements SpecialModelReader {
        @Override
        public SpecialModel read(JsonObject json) {
            String pose = json.get("pose").getAsString();
            String texture = json.get("texture").getAsString();
            return new CopperGolemStatueSpecialModel(pose, texture);
        }
    }
}
