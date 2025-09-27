package net.momirealms.craftengine.core.item.behavior;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public class EmptyItemBehavior extends ItemBehavior {
    public static final Factory FACTORY = new Factory();
    public static final EmptyItemBehavior INSTANCE = new EmptyItemBehavior();

    public static class Factory implements ItemBehaviorFactory {

        @Override
        public ItemBehavior create(Pack pack, Path path, String node, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
