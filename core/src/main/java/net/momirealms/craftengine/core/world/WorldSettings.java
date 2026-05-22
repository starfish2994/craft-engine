package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Tristate;
import net.momirealms.sparrow.nbt.CompoundTag;

public final class WorldSettings {
    private static final String RESTORE_CUSTOM_BLOCK = "restore_custom_blocks_on_chunk_load";
    private static final String SYNC_CUSTOM_BLOCK = "sync_custom_blocks_on_chunk_load";
    private static final String RESTORE_VANILLA_BLOCK = "restore_vanilla_blocks_on_chunk_unload";
    private final CompoundTag settings;
    public boolean restoreCustomBlocksOnChunkLoad;
    public boolean syncCustomBlocksOnChunkLoad;
    public boolean restoreVanillaBlocksOnChunkLoad;

    public WorldSettings() {
        this(new CompoundTag());
    }

    public WorldSettings(CompoundTag settings) {
        this.settings = settings;
        this.restoreCustomBlocksOnChunkLoad = settings.getBoolean(RESTORE_CUSTOM_BLOCK, Config.restoreCustomBlocks());
        this.syncCustomBlocksOnChunkLoad = settings.getBoolean(SYNC_CUSTOM_BLOCK, Config.syncCustomBlocks());
        this.restoreVanillaBlocksOnChunkLoad = settings.getBoolean(RESTORE_VANILLA_BLOCK, Config.restoreVanillaBlocks());
    }

    public void set(String node, Tristate value) {
        switch (node) {
            case RESTORE_CUSTOM_BLOCK -> setRestoreCustomBlocksOnChunkLoad(value);
            case SYNC_CUSTOM_BLOCK -> setSyncCustomBlocksOnChunkLoad(value);
            case RESTORE_VANILLA_BLOCK -> setRestoreVanillaBlocksOnChunkUnload(value);
        }
    }

    public void setSyncCustomBlocksOnChunkLoad(Tristate state) {
        if (state == Tristate.UNDEFINED) {
            this.syncCustomBlocksOnChunkLoad = Config.syncCustomBlocks();
            this.settings.remove(SYNC_CUSTOM_BLOCK);
        } else {
            this.settings.putBoolean(SYNC_CUSTOM_BLOCK, state.asBoolean());
            this.syncCustomBlocksOnChunkLoad = state.asBoolean();
        }
    }

    public void setRestoreCustomBlocksOnChunkLoad(Tristate state) {
        if (state == Tristate.UNDEFINED) {
            this.restoreCustomBlocksOnChunkLoad = Config.restoreCustomBlocks();
            this.settings.remove(RESTORE_CUSTOM_BLOCK);
        } else {
            this.settings.putBoolean(RESTORE_CUSTOM_BLOCK, state.asBoolean());
            this.restoreCustomBlocksOnChunkLoad = state.asBoolean();
        }
    }

    public void setRestoreVanillaBlocksOnChunkUnload(Tristate state) {
        if (state == Tristate.UNDEFINED) {
            this.restoreVanillaBlocksOnChunkLoad = Config.restoreVanillaBlocks();
            this.settings.remove(RESTORE_VANILLA_BLOCK);
        } else {
            this.settings.putBoolean(RESTORE_VANILLA_BLOCK, state.asBoolean());
            this.restoreVanillaBlocksOnChunkLoad = state.asBoolean();
        }
    }

    public CompoundTag tag() {
        return this.settings;
    }
}
