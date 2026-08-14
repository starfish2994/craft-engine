package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PalettedPermutationsSource(List<Key> textures, String separator, Key paletteKey,
                                         Map<String, Key> permutations) implements SpriteSource {

    public static final String DEFAULT_SEPARATOR = "_";
    private static final String[] TEXTURES = ConfigKeys.of("texture(s)");
    private static final String[] PALETTE_KEY = ConfigKeys.of("palette_key");
    private static final String[] PERMUTATIONS = ConfigKeys.of("permutation(s)");

    public PalettedPermutationsSource {
        textures = List.copyOf(textures);
        permutations = new LinkedHashMap<>(permutations);
    }

    static @Nullable PalettedPermutationsSource fromJson(JsonObject json) {
        JsonArray texturesJson = json.getAsJsonArray("textures");
        if (texturesJson == null || !json.has("palette_key")) return null;
        List<Key> textures = new ArrayList<>();
        for (JsonElement element : texturesJson) {
            textures.add(Key.of(element.getAsString()));
        }
        String separator = json.has("separator") ? json.get("separator").getAsString() : DEFAULT_SEPARATOR;
        Key paletteKey = Key.of(json.get("palette_key").getAsString());
        Map<String, Key> permutations = new LinkedHashMap<>();
        if (json.get("permutations") instanceof JsonObject permutationsJson) {
            for (Map.Entry<String, JsonElement> entry : permutationsJson.entrySet()) {
                permutations.put(entry.getKey(), Key.of(entry.getValue().getAsString()));
            }
        }
        return new PalettedPermutationsSource(textures, separator, paletteKey, permutations);
    }

    static PalettedPermutationsSource fromConfig(ConfigSection config) {
        List<Key> textures = config.getNonEmptyList(TEXTURES, value -> Key.of(value.getAsString()));
        Key paletteKey = config.getNonNullKey(PALETTE_KEY);
        String separator = config.getString("separator", DEFAULT_SEPARATOR);
        Map<String, Key> permutations = new LinkedHashMap<>();
        ConfigSection permutationsSection = config.getSection(PERMUTATIONS);
        if (permutationsSection != null) {
            for (String suffix : permutationsSection.keySet()) {
                permutations.put(suffix, permutationsSection.getNonNullKey(suffix));
            }
        }
        return new PalettedPermutationsSource(textures, separator, paletteKey, permutations);
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "paletted_permutations");
        JsonArray textures = new JsonArray();
        for (Key texture : this.textures) {
            textures.add(texture.toString());
        }
        json.add("textures", textures);
        if (!DEFAULT_SEPARATOR.equals(this.separator)) {
            json.addProperty("separator", this.separator);
        }
        json.addProperty("palette_key", this.paletteKey.toString());
        JsonObject permutations = new JsonObject();
        for (Map.Entry<String, Key> entry : this.permutations.entrySet()) {
            permutations.addProperty(entry.getKey(), entry.getValue().toString());
        }
        json.add("permutations", permutations);
        return json;
    }
}
