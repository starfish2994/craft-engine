package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public final class CubeBottomTopBlockModelReader implements SimplifiedBlockModelReader {
    public static final CubeBottomTopBlockModelReader INSTANCE = new CubeBottomTopBlockModelReader();
    private static final Key PARENT = Key.of("minecraft:block/cube_bottom_top");

    private CubeBottomTopBlockModelReader() {
    }

    @Override
    public ModelGeneration read(@UnknownNullability List<Key> textures) {
        return ModelGeneration.builder()
                .parentModelPath(PARENT)
                .texturesOverride(
                        Map.of(
                                "bottom", textures.getFirst().asMinimalString(),
                                "side", textures.get(1).asMinimalString(),
                                "top", textures.get(2).asMinimalString()
                        )
                )
                .build();
    }
}
