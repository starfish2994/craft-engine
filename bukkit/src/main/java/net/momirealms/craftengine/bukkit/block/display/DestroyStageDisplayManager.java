package net.momirealms.craftengine.bukkit.block.display;

import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.block.setting.DestroyStageDisplay;
import net.momirealms.craftengine.core.world.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DestroyStageDisplayManager {
    private static final DestroyStageDisplayManager INSTANCE = new DestroyStageDisplayManager();
    private final Map<PosKey, Entry> active = new HashMap<>();

    private DestroyStageDisplayManager() {
    }

    public static DestroyStageDisplayManager instance() {
        return INSTANCE;
    }

    public record PosKey(UUID world, long pos) {
    }

    public static final class Entry {
        private final PosKey key;
        private final DestroyStageDisplay display;
        private final DestroyStageDisplayEntity entity;
        private final Map<UUID, Float> minerProgress = Collections.synchronizedMap(new HashMap<>());
        private int displayedIndex = -1;

        private Entry(PosKey key, DestroyStageDisplay display, DestroyStageDisplayEntity entity) {
            this.key = key;
            this.display = display;
            this.entity = entity;
        }

        public DestroyStageDisplayEntity entity() {
            return this.entity;
        }

        public DestroyStageDisplay display() {
            return this.display;
        }

        public PosKey key() {
            return this.key;
        }

        public Map<UUID, Float> minerProgress() {
            return this.minerProgress;
        }

        public int displayedIndex() {
            return this.displayedIndex;
        }

        public void displayedIndex(int index) {
            this.displayedIndex = index;
        }
    }

    public Entry getOrCreate(PosKey key, DestroyStageDisplay display, BlockPos pos) {
        Entry entry = this.active.get(key);
        if (entry == null) {
            DestroyStageDisplayEntity entity = new DestroyStageDisplayEntity(display, EntityUtils.ENTITY_COUNTER.incrementAndGet(), pos);
            entry = new Entry(key, display, entity);
            this.active.put(key, entry);
        }
        return entry;
    }

    public Entry get(PosKey key) {
        return this.active.get(key);
    }

    public void remove(PosKey key) {
        Entry entry = this.active.remove(key);
        if (entry != null) {
            Object removePacket = entry.entity.removePacket();
            for (UUID viewerId : entry.entity.spawnedViewers()) {
                BukkitServerPlayer serverPlayer = (BukkitServerPlayer) BukkitNetworkManager.instance().getOnlineUser(viewerId);
                if (serverPlayer == null) continue;
                serverPlayer.sendPacket(removePacket, false);
            }
        }
    }
}
