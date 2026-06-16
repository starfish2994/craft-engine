package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public final class CubeColumnBlockModelReader implements SimplifiedBlockModelReader {
    public static final CubeColumnBlockModelReader INSTANCE = new CubeColumnBlockModelReader();
    private static final Key PARENT = Key.of("minecraft:block/cube_column");

    private CubeColumnBlockModelReader() {
    }

    @Override
    public ModelGeneration read(@UnknownNullability List<Key> textures) {
        return ModelGeneration.builder()
                .parentModelPath(PARENT)
                .texturesOverride(
                        Map.of(
                                "end", textures.getFirst().asMinimalString(),
                                "side", textures.get(1).asMinimalString()
                        )
                )
                .build();
    }
}
