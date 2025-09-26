package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.util.Key;

public interface BlockStateWrapper {

    Object literalObject();

    int registryId();

    Key ownerId();
}
