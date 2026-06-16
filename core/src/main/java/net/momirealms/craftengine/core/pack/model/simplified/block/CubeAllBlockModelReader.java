package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public final class CubeAllBlockModelReader implements SimplifiedBlockModelReader {
    public static final CubeAllBlockModelReader INSTANCE = new CubeAllBlockModelReader();
    private static final Key PARENT = Key.of("minecraft:block/cube_all");

    private CubeAllBlockModelReader() {
    }

    @Override
    public ModelGeneration read(@UnknownNullability List<Key> textures) {
        return ModelGeneration.builder()
                .parentModelPath(PARENT)
                .texturesOverride(Map.of("all", textures.getFirst().asMinimalString()))
                .build();
    }
}
