package net.momirealms.craftengine.core.attribute.equipment;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.attribute.AttributeInstance;
import net.momirealms.craftengine.core.attribute.AttributeManager;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.item.equipment.EquipmentSet;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.item.setting.value.EquipmentSetPart;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EntityEquipments {
    private final Map<EquipmentSetSlot, EquipmentSlotItem> equipments = new Object2ObjectOpenHashMap<>();
    private final AttributeContainer container;
    private Map<Key, Integer> activeSets = Map.of();
    private boolean dirty;

    public EntityEquipments(AttributeContainer container) {
        this.container = container;
    }

    @Nullable
    public EquipmentSlotItem get(EquipmentSetSlot slot) {
        return this.equipments.get(slot);
    }

    @Nullable
    public EquipmentSlotItem add(EquipmentSetSlot slot, Item item) {
        EquipmentSlotItem newItem = EquipmentSlotItem.create(slot, item);
        EquipmentSlotItem previous = this.equipments.put(slot, newItem);
        if (previous != null) {
            previous.removeModifiers(this.container);
        }
        newItem.addOrUpdateModifiers(this.container);
        this.dirty = true;
        return previous;
    }

    @Nullable
    public EquipmentSlotItem remove(EquipmentSetSlot slot) {
        EquipmentSlotItem removed = this.equipments.remove(slot);
        if (removed != null) {
            removed.removeModifiers(this.container);
        }
        this.dirty = true;
        return removed;
    }

    public void updateSets() {
        if (!this.dirty) return;
        this.dirty = false;
        Map<Key, Integer> raw = computeRawActiveSets();
        Map<Key, Integer> previous = this.activeSets;
        if (raw.equals(previous)) {
            this.activeSets = raw;
            return;
        }
        this.activeSets = raw;
        AttributeManager manager = CraftEngine.instance().attributeManager();
        Set<Key> names = new HashSet<>(previous.keySet());
        names.addAll(raw.keySet());
        for (Key name : names) {
            int oldPieces = previous.getOrDefault(name, 0);
            int newPieces = raw.getOrDefault(name, 0);
            if (oldPieces == newPieces) continue;
            manager.equipmentSet(name).ifPresent(set -> applySetChange(set, oldPieces, newPieces));
        }
    }

    private void applySetChange(EquipmentSet set, int oldPieces, int newPieces) {
        for (int tier = 1; tier <= set.maxTier(); tier++) {
            boolean wasActive = set.isTierActive(tier, oldPieces);
            boolean isActive = set.isTierActive(tier, newPieces);
            if (wasActive == isActive) continue;
            EquipmentSet.Entry entry = set.entry(tier);
            if (entry == null) continue;
            if (isActive) {
                activateEntry(entry);
            } else {
                deactivateEntry(entry);
            }
        }
    }

    private void activateEntry(EquipmentSet.Entry entry) {
        for (AttributeModifierConfig config : entry.modifiers()) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = this.container.getInstance(config.attribute);
            if (instance != null) {
                instance.addOrUpdateModifier(config.build());
            }
        }
        List<SetPotionEffect> potionEffects = entry.potionEffects();
        if (!potionEffects.isEmpty() && this.container.entity() instanceof LivingEntity livingEntity) {
            for (SetPotionEffect effect : potionEffects) {
                livingEntity.addPotionEffect(effect.type(), effect.duration(), effect.amplifier(), effect.ambient(), effect.particles(), effect.icon());
            }
        }
        entry.onActivate().run(this.container.context());
    }

    private void deactivateEntry(EquipmentSet.Entry entry) {
        for (AttributeModifierConfig config : entry.modifiers()) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = this.container.getInstance(config.attribute);
            if (instance != null) {
                instance.removeModifier(config.id);
            }
        }
        List<SetPotionEffect> potionEffects = entry.potionEffects();
        if (!potionEffects.isEmpty() && this.container.entity() instanceof LivingEntity livingEntity) {
            for (SetPotionEffect effect : potionEffects) {
                livingEntity.removePotionEffect(effect.type());
            }
        }
        entry.onDeactivate().run(this.container.context());
    }

    // 容器销毁(退出/实体移除)时清除套装施加的药水;修饰符随容器废弃,触发器不跑
    public void clearSetEffects() {
        Map<Key, Integer> active = this.activeSets;
        if (active.isEmpty()) return;
        this.activeSets = Map.of();
        this.dirty = false;
        if (!(this.container.entity() instanceof LivingEntity livingEntity)) return;
        AttributeManager manager = CraftEngine.instance().attributeManager();
        for (Map.Entry<Key, Integer> e : active.entrySet()) {
            manager.equipmentSet(e.getKey()).ifPresent(set -> {
                int pieces = e.getValue();
                for (int tier = 1; tier <= set.maxTier(); tier++) {
                    if (!set.isTierActive(tier, pieces)) continue;
                    EquipmentSet.Entry entry = set.entry(tier);
                    if (entry == null) continue;
                    for (SetPotionEffect effect : entry.potionEffects()) {
                        livingEntity.removePotionEffect(effect.type());
                    }
                }
            });
        }
    }

    public Map<Key, Integer> getRawActiveSets() {
        updateSets();
        return this.activeSets;
    }

    private Map<Key, Integer> computeRawActiveSets() {
        if (this.equipments.isEmpty()) return Map.of();
        Map<Key, Integer> setPartCount = new HashMap<>();
        for (Map.Entry<EquipmentSetSlot, EquipmentSlotItem> entry : this.equipments.entrySet()) {
            Item item = entry.getValue().item();
            Optional<ItemDefinition> definition = item.getDefinition();
            definition.ifPresent(def -> {
                EquipmentSetPart equipmentSetPart = def.settings().equipmentSetPart();
                if (equipmentSetPart != null) {
                    List<Key> matchingSets = equipmentSetPart.getMatchingSets(entry.getKey());
                    for (Key set : matchingSets) {
                        setPartCount.merge(set, 1, Integer::sum);
                    }
                }
            });
        }
        return setPartCount;
    }
}
