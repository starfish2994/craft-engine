package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.customdata.CustomDataKey;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.util.CustomDataSerializer;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class EquipmentPotionEffectStateStore {
    private static final CustomDataKey<Map<Key, ManagedPotionEffectState>> DATA_KEY = new CustomDataKey<>(
            Key.ce("equipment_potion_effect_state"),
            new CustomDataSerializer<>() {
                @Override
                public Tag serialize(Map<Key, ManagedPotionEffectState> states) {
                    CompoundTag root = new CompoundTag();
                    for (Map.Entry<Key, ManagedPotionEffectState> entry : states.entrySet()) {
                        root.put(entry.getKey().asString(), save(entry.getValue()));
                    }
                    return root;
                }

                @Override
                public Map<Key, ManagedPotionEffectState> deserialize(Tag tag) {
                    if (!(tag instanceof CompoundTag root)) return Map.of();
                    Map<Key, ManagedPotionEffectState> states = new HashMap<>();
                    for (Map.Entry<String, Tag> entry : root.entrySet()) {
                        if (!(entry.getValue() instanceof CompoundTag stateTag)) continue;
                        Key type = Key.of(entry.getKey());
                        ManagedPotionEffectState state = readState(type, stateTag);
                        if (state != null) {
                            states.put(type, state);
                        }
                    }
                    return states;
                }
            }
    );

    private final Entity entity;

    public EquipmentPotionEffectStateStore(Entity entity) {
        this.entity = entity;
    }

    private static CompoundTag save(ManagedPotionEffectState state) {
        CompoundTag tag = new CompoundTag();
        tag.put("managed", writeManaged(state.managed()));
        if (state.shadow() != null) {
            tag.put("shadow", writeSnapshot(state.shadow()));
        }
        return tag;
    }

    @Nullable
    private static ManagedPotionEffectState readState(Key type, CompoundTag tag) {
        CompoundTag managed = tag.getCompound("managed");
        if (managed == null) return null;
        CompoundTag shadow = tag.getCompound("shadow");
        return new ManagedPotionEffectState(
                readManaged(type, managed),
                shadow == null ? null : readSnapshot(type, shadow)
        );
    }

    private static CompoundTag writeManaged(SetPotionEffect effect) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("amplifier", effect.amplifier());
        tag.putBoolean("ambient", effect.ambient());
        tag.putBoolean("particles", effect.particles());
        tag.putBoolean("icon", effect.icon());
        return tag;
    }

    private static SetPotionEffect readManaged(Key type, CompoundTag tag) {
        return new SetPotionEffect(
                type,
                tag.getInt("amplifier", 0),
                tag.getBoolean("ambient", false),
                tag.getBoolean("particles", true),
                tag.getBoolean("icon", true)
        );
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

    public Map<Key, ManagedPotionEffectState> load() {
        Map<Key, ManagedPotionEffectState> states = this.entity.getCustomData(DATA_KEY);
        return states == null ? Map.of() : states;
    }

    public void save(Map<Key, ManagedPotionEffectState> states) {
        if (states.isEmpty()) {
            this.entity.removeCustomData(DATA_KEY);
        } else {
            this.entity.setCustomData(DATA_KEY, states);
        }
    }
}
