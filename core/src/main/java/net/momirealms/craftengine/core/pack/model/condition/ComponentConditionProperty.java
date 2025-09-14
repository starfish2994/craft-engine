package net.momirealms.craftengine.core.pack.model.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class ComponentConditionProperty implements ConditionProperty {
    public static final Factory FACTORY = new Factory();
    public static final Reader READER = new Reader();
    private final String predicate;
    private final JsonElement value;

    public ComponentConditionProperty(String predicate, JsonElement value) {
        this.predicate = predicate;
        this.value = value;
    }

    @Override
    public Key type() {
        return ConditionProperties.COMPONENT;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", type().toString());
        jsonObject.addProperty("predicate", this.predicate);
        jsonObject.add("value", this.value);
    }

    public static class Factory implements ConditionPropertyFactory {
        @Override
        public ConditionProperty create(Map<String, Object> arguments) {
            String predicate = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("predicate"), "warning.config.item.model.condition.component.missing_predicate");
            JsonElement jsonElement = GsonHelper.get().toJsonTree(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("value"), "warning.config.item.model.condition.component.missing_value"));
            return new ComponentConditionProperty(predicate, jsonElement);
        }
    }

    public static class Reader implements ConditionPropertyReader {
        @Override
        public ConditionProperty read(JsonObject json) {
            String predicate = json.get("predicate").getAsString();
            JsonElement value = json.get("value");
            return new ComponentConditionProperty(predicate, value);
        }
    }
}
