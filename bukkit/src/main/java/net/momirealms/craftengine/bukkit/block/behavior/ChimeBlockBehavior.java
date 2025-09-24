package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ChimeBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final List<Pair<SoundData, Float>> hitSounds;

    public ChimeBlockBehavior(CustomBlock customBlock, List<Pair<SoundData, Float>> hitSounds) {
        super(customBlock);
        this.hitSounds = hitSounds;
    }

    @Override
    public void onProjectileHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (hitSounds.isEmpty()) return;
        Pair<SoundData, Float> hitSound = hitSounds.get(RandomUtils.generateRandomInt(0, hitSounds.size()));
        Object blockPos = FastNMS.INSTANCE.field$BlockHitResult$blockPos(args[2]);
        Object sound = FastNMS.INSTANCE.constructor$SoundEvent(KeyUtils.toResourceLocation(hitSound.left().id()), Optional.empty());
        float pitch = hitSound.left().pitch().get() + RandomUtils.generateRandomInt(0, 1) * hitSound.right();
        FastNMS.INSTANCE.method$LevelAccessor$playSound(args[0], null, blockPos, sound, CoreReflections.instance$SoundSource$BLOCKS, hitSound.left().volume().get(), pitch);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            List<Pair<SoundData, Float>> hitSounds = ResourceConfigUtils.parseConfigAsList(arguments.get("hit-sounds"), map -> {
                SoundData hitSound = SoundData.create(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("sound"), "warning.config.block.behavior.chime.missing_hit_sound"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f));
                float randomMultiplier = ResourceConfigUtils.getAsFloat(arguments.get("random-pitch-multiplier"), "random-pitch-multiplier");
                return Pair.of(hitSound, randomMultiplier);
            });
            return new ChimeBlockBehavior(block, hitSounds);
        }
    }
}
