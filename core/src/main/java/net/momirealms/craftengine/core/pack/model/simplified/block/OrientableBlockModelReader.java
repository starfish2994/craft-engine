package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public final class OrientableBlockModelReader implements SimplifiedBlockModelReader {
    public static final OrientableBlockModelReader INSTANCE = new OrientableBlockModelReader();
    private static final Key PARENT = Key.of("minecraft:block/orientable");

    private OrientableBlockModelReader() {
    }

    @Override
    public ModelGeneration read(@UnknownNullability List<Key> textures) {
        return ModelGeneration.builder()
                .parentModelPath(PARENT)
                .texturesOverride(
                        Map.of(
                                "bottom", textures.get(0).asMinimalString(),
                                "front", textures.get(1).asMinimalString(),
                                "side", textures.get(2).asMinimalString(),
                                "top", textures.get(3).asMinimalString()
                        )
                )
                .build();
    }
}
