package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.parser.BlockStateParser;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class StrippableBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final String stripped;
    private final LazyReference<ImmutableBlockState> lazyState;

    public StrippableBlockBehavior(CustomBlock block, String stripped) {
        super(block);
        this.stripped = stripped;
        this.lazyState = LazyReference.lazyReference(() -> BlockStateParser.deserialize(this.stripped));
    }

    public String stripped() {
        return this.stripped;
    }

    public ImmutableBlockState strippedState() {
        return this.lazyState.get();
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            String stripped = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("stripped"), "warning.config.block.behavior.strippable.missing_stripped");
            return new StrippableBlockBehavior(block, stripped);
        }
    }
}
