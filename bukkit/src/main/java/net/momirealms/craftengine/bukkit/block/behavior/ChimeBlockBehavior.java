package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ChimeBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final SoundData hitSound;
    private final boolean randomPitch;
    private final float randomMultiplier;

    public ChimeBlockBehavior(CustomBlock customBlock, SoundData hitSound, boolean randomPitch, float randomMultiplier) {
        super(customBlock);
        this.hitSound = hitSound;
        this.randomPitch = randomPitch;
        this.randomMultiplier = randomMultiplier;
    }

    @Override
    public void onProjectileHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockPos = FastNMS.INSTANCE.field$BlockHitResult$blockPos(args[2]);
        Object sound = FastNMS.INSTANCE.constructor$SoundEvent(KeyUtils.toResourceLocation(hitSound.id()), Optional.empty());
        float pitch = hitSound.pitch().get();
        if (randomPitch) {
            pitch = pitch + RandomUtils.generateRandomInt(0, 1) * this.randomMultiplier;
        }
        FastNMS.INSTANCE.method$LevelAccessor$playSound(args[0], null, blockPos, sound, CoreReflections.instance$SoundSource$BLOCKS, hitSound.volume().get(), pitch);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            SoundData hitSound = SoundData.create(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("hit-sound"), "warning.config.block.behavior.chime.missing_hit_sound"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f));
            Map<String, Object> randomPitch = ResourceConfigUtils.getAsMapOrNull(arguments.get("random-pitch"), "random-pitch");
            boolean enableRandomPitch = false;
            float randomMultiplier = 1f;
            if (randomPitch != null) {
                enableRandomPitch = true;
                randomMultiplier = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("multiplier", 1f), "multiplier");
            }
            return new ChimeBlockBehavior(block, hitSound, enableRandomPitch, randomMultiplier);
        }
    }
}
