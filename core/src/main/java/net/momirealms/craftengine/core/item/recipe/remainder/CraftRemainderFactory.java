package net.momirealms.craftengine.core.item.recipe.remainder;

import java.util.Map;

public interface CraftRemainderFactory {

    CraftRemainder create(Map<String, Object> args);
}
