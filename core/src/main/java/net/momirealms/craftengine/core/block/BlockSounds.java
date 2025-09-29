package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.sound.SoundData;

import java.util.Map;

public final class BlockSounds {
    /*
    Fall 0.5 0.75
    Place 1 0.8
    Step 0.15 1
    Hit 0.5 0.5
    Break 1 0.8
    Land 0.3 1
    Destroy 1 1
     */
    public static final BlockSounds EMPTY = new BlockSounds(SoundData.EMPTY, SoundData.EMPTY, SoundData.EMPTY, SoundData.EMPTY, SoundData.EMPTY);

    private final SoundData breakSound;
    private final SoundData stepSound;
    private final SoundData placeSound;
    private final SoundData hitSound;
    private final SoundData fallSound;

    public BlockSounds(SoundData breakSound, SoundData stepSound, SoundData placeSound, SoundData hitSound, SoundData fallSound) {
        this.breakSound = breakSound;
        this.stepSound = stepSound;
        this.placeSound = placeSound;
        this.hitSound = hitSound;
        this.fallSound = fallSound;
    }

    public static BlockSounds fromMap(Map<String, Object> map) {
        if (map == null) return EMPTY;
        return new BlockSounds(
                SoundData.create(map.getOrDefault("break", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                SoundData.create(map.getOrDefault("step", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_0_15, SoundData.SoundValue.FIXED_1),
                SoundData.create(map.getOrDefault("place", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                SoundData.create(map.getOrDefault("hit", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_5),
                SoundData.create(map.getOrDefault("fall", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_75)
        );
    }

    public SoundData breakSound() {
        return breakSound;
    }

    public SoundData stepSound() {
        return stepSound;
    }

    public SoundData placeSound() {
        return placeSound;
    }

    public SoundData hitSound() {
        return hitSound;
    }

    public SoundData fallSound() {
        return fallSound;
    }
}
