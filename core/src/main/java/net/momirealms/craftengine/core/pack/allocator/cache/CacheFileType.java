package net.momirealms.craftengine.core.pack.allocator.cache;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.GsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface CacheFileType<T> {
    JsonCacheFileType JSON = new JsonCacheFileType();

    T read(Path path) throws IOException;

    void write(Path path, T value) throws IOException;

    T create();

    class JsonCacheFileType implements CacheFileType<JsonObject> {

        @Override
        public JsonObject read(Path path) throws IOException {
            if (Files.exists(path)) {
                return GsonHelper.readJsonFile(path).getAsJsonObject();
            }
            return new JsonObject();
        }

        @Override
        public void write(Path path, JsonObject value) throws IOException {
            GsonHelper.writeJsonFile(value, path);
        }

        @Override
        public JsonObject create() {
            return new JsonObject();
        }
    }
}
