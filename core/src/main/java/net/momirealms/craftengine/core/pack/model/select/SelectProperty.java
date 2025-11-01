package net.momirealms.craftengine.core.pack.model.select;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface SelectProperty extends Consumer<JsonObject> {

    Key type();

    default List<Revision> revisions(JsonElement element) {
        return Collections.emptyList();
    }

    @Nullable
    default JsonElement remap(JsonElement element, MinecraftVersion version) {
        return element;
    }
}
