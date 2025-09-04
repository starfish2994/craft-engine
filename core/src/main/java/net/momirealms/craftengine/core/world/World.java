package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.particle.ParticleData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.UUID;

public interface World {

    Object platformWorld();

    Object serverWorld();

    WorldHeight worldHeight();

    ExistingBlock getBlockAt(int x, int y, int z);

    default ExistingBlock getBlockAt(final BlockPos pos) {
        return getBlockAt(pos.x(), pos.y(), pos.z());
    }

    void setBlockAt(int x, int y, int z, BlockStateWrapper blockState, int flags);

    String name();

    Path directory();

    UUID uuid();

    void dropItemNaturally(Position location, Item<?> item);

    void dropExp(Position location, int amount);

    void playSound(Position location, Key sound, float volume, float pitch, SoundSource source);

    void playBlockSound(Position location, Key sound, float volume, float pitch);

    default void playBlockSound(Position location, SoundData data) {
        playBlockSound(location, data.id(), data.volume().get(), data.pitch().get());
    }

    void levelEvent(int id, BlockPos pos, int data);

    void spawnParticle(Position location, Key particle, int count, double xOffset, double yOffset, double zOffset, double speed, @Nullable ParticleData extraData, @NotNull Context context);

    long time();
}
