package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.customdata.CustomDataKey;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.util.CustomDataSerializer;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public final class PotionEffectStateStore {
    private static final CustomDataKey<Map<Key, PotionEffectSnapshot>> DATA_KEY = new CustomDataKey<>(
            Key.ce("equipment_potion_effect_state"),
            new CustomDataSerializer<>() {
                @Override
                public Tag serialize(Map<Key, PotionEffectSnapshot> states) {
                    CompoundTag root = new CompoundTag();
                    for (Map.Entry<Key, PotionEffectSnapshot> entry : states.entrySet()) {
                        root.put(entry.getKey().asString(), writeSnapshot(entry.getValue()));
                    }
                    return root;
                }

                @Override
                public Map<Key, PotionEffectSnapshot> deserialize(Tag tag) {
                    if (!(tag instanceof CompoundTag root)) return Map.of();
                    Map<Key, PotionEffectSnapshot> states = new HashMap<>();
                    for (Map.Entry<String, Tag> entry : root.entrySet()) {
                        if (!(entry.getValue() instanceof CompoundTag stateTag)) continue;
                        Key type = Key.of(entry.getKey());
                        // Old data wrapped the shadow beside a managed fingerprint.
                        // Read that shape once, but always write the flat shadow-only form.
                        CompoundTag snapshotTag = stateTag.getCompound("shadow");
                        if (snapshotTag == null && stateTag.containsKey("duration")) {
                            snapshotTag = stateTag;
                        }
                        if (snapshotTag != null) states.put(type, readSnapshot(type, snapshotTag));
                    }
                    return states;
                }
            }
    );

    private final Entity entity;
    private boolean hasStoredData;

    public PotionEffectStateStore(Entity entity) {
        this.entity = entity;
    }

    private static CompoundTag writeSnapshot(PotionEffectSnapshot effect) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("duration", effect.duration());
        tag.putInt("amplifier", effect.amplifier());
        tag.putBoolean("ambient", effect.ambient());
        tag.putBoolean("particles", effect.particles());
        tag.putBoolean("icon", effect.showIcon());
        return tag;
    }

    private static PotionEffectSnapshot readSnapshot(Key type, CompoundTag tag) {
        return new PotionEffectSnapshot(
                type,
                tag.getInt("duration", 0),
                tag.getInt("amplifier", 0),
                tag.getBoolean("ambient", false),
                tag.getBoolean("particles", true),
                tag.getBoolean("icon", true)
        );
    }

    public Map<Key, PotionEffectSnapshot> load() {
        Map<Key, PotionEffectSnapshot> states = this.entity.getCustomData(DATA_KEY);
        this.hasStoredData = states != null;
        return states == null ? Map.of() : states;
    }

    public void save(Map<Key, PotionEffectSnapshot> states) {
        if (states.isEmpty()) {
            if (!this.hasStoredData) return;
            this.entity.removeCustomData(DATA_KEY);
            this.hasStoredData = false;
        } else {
            this.entity.setCustomData(DATA_KEY, states);
            this.hasStoredData = true;
        }
    }
}
