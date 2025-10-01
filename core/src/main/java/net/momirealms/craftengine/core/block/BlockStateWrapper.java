package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

public interface BlockStateWrapper extends Comparable<BlockStateWrapper> {

    Object literalObject();

    int registryId();

    Key ownerId();

    <T> T getProperty(String propertyName);

    boolean hasProperty(String propertyName);

    String getAsString();

    @Override
    default int compareTo(@NotNull BlockStateWrapper o) {
        return Integer.compare(registryId(), o.registryId());
    }
}
