package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.EmptyBlockDefinition;

public class EmptyBlockBehavior extends BukkitBlockBehavior {
    public static final EmptyBlockBehavior INSTANCE = new EmptyBlockBehavior();

    public EmptyBlockBehavior() {
        super(EmptyBlockDefinition.INSTANCE);
    }
}
