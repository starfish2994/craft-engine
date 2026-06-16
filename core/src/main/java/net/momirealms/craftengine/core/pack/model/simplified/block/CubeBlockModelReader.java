package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public final class CubeBlockModelReader implements SimplifiedBlockModelReader {
    public static final CubeBlockModelReader INSTANCE = new CubeBlockModelReader();
    private static final Key PARENT = Key.of("minecraft:block/cube");

    private CubeBlockModelReader() {
    }

    @Override
    public ModelGeneration read(@UnknownNullability List<Key> textures) {
        return ModelGeneration.builder()
                .parentModelPath(PARENT)
                .texturesOverride(
                        Map.of(
                                "down", textures.getFirst().asMinimalString(),
                                "up", textures.get(1).asMinimalString(),
                                "north", textures.get(2).asMinimalString(),
                                "south", textures.get(3).asMinimalString(),
                                "west", textures.get(4).asMinimalString(),
                                "east", textures.get(5).asMinimalString(),
                                "particle", textures.get(Math.max(textures.size() - 1, 5)).asMinimalString()
                        )
                )
                .build();
    }
}
