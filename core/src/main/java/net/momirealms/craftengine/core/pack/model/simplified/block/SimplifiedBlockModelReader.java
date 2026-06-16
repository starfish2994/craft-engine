package net.momirealms.craftengine.core.pack.model.simplified.block;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public interface SimplifiedBlockModelReader {

    ModelGeneration read(List<Key> texture);
}
