package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.util.Key;

public interface BlockStateWrapper {

    Object literalObject();

    int registryId();

    Key ownerId();

    <T> T getProperty(String propertyName);

    boolean hasProperty(String propertyName);

    String getAsString();
}
