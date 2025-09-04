package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.plugin.reflection.bukkit.CraftBukkitReflections;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ChangeOverTimeBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final float changeSpeed;
    private final Key nextBlock;

    public ChangeOverTimeBlockBehavior(CustomBlock customBlock, float changeSpeed, Key nextBlock) {
        super(customBlock);
        this.changeSpeed = changeSpeed;
        this.nextBlock = nextBlock;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws ReflectiveOperationException {
        if (RandomUtils.generateRandomFloat(0F, 1F) >= this.changeSpeed) return;
        Optional<Object> nextState = BukkitBlockManager.instance().blockById(this.nextBlock)
                .map(CustomBlock::defaultState)
                .map(ImmutableBlockState::customBlockState)
                .map(BlockStateWrapper::literalObject);
        if (nextState.isEmpty()) return;
        CraftBukkitReflections.method$CraftEventFactory$handleBlockFormEvent.invoke(null, args[1], args[2], nextState.get(), UpdateOption.UPDATE_ALL.flags());
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            float changeSpeed = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("change-speed", 0.05688889F), "change-speed");
            Key nextBlock = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.getOrDefault("next-block", "minecraft:air"), "warning.config.block.behavior.change_over_time.missing_next_block"));
            return new ChangeOverTimeBlockBehavior(block, changeSpeed, nextBlock);
        }
    }
}
