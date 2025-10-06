package net.momirealms.craftengine.core.pack.allocator.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public interface CacheSerializer<T extends Comparable<T>, A> {

    A serialize(Map<T, String> obj);

    Map<String, T> deserialize(A obj);

    static <T extends Comparable<T>> CacheSerializer<T, JsonObject> json() {
        return new JsonSerializer<>();
    }

    class JsonSerializer<T extends Comparable<T>> implements CacheSerializer<T, JsonObject> {

        @Override
        public JsonObject serialize(Map<T, String> obj) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<T, String> entry : obj.entrySet()) {
                if (entry.getKey() instanceof Integer i) {
                    jsonObject.addProperty(entry.getValue(), i);
                } else if (entry.getKey() instanceof String s) {
                    jsonObject.addProperty(entry.getValue(), s);
                }
            }
            return jsonObject;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, T> deserialize(JsonObject obj) {
            Map<String, T> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                if (entry.getValue() instanceof JsonPrimitive primitive) {
                    if (primitive.isNumber()) {
                        map.put(entry.getKey(), (T) (Integer) primitive.getAsInt());
                    } else if (primitive.isString()) {
                        map.put(entry.getKey(), (T) primitive.getAsString());
                    }
                }
            }
            return map;
        }
    }
}
