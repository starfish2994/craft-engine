package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.attribute.damage.EntityDamageListener;
import net.momirealms.craftengine.bukkit.attribute.damage.PaperAttackStrengthListener;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.attribute.AbstractAttributeManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.core.RegistryProxy;
import net.momirealms.craftengine.proxy.minecraft.core.registries.BuiltInRegistriesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes.AttributeSupplierProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes.DefaultAttributesProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BukkitAttributeManager extends AbstractAttributeManager {
    private static BukkitAttributeManager instance;
    private final BukkitCraftEngine plugin;
    private final AttributeEventListener attributeEventListener;
    private final EntityDamageListener entityDamageListener;
    private final PaperAttackStrengthListener attackStrengthListener;
    private final Map<Key, Map<Key, Double>> vanillaDefaultAttributes;

    public BukkitAttributeManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        this.attributeEventListener = new AttributeEventListener();
        this.entityDamageListener = new EntityDamageListener(this);
        this.attackStrengthListener = VersionHelper.hasPaperPatch ? new PaperAttackStrengthListener() : null;
        this.vanillaDefaultAttributes = buildVanillaDefaultAttributeTable();
        instance = this;
    }

    public static BukkitAttributeManager instance() {
        return instance;
    }

    @Override
    public void delayedInit() {
        buildVanillaDefaultAttributeTable();
    }

    @Override
    public void runDelayedSyncTasks() {
        boolean enable = Config.enableAttributeSystem();
        this.attributeEventListener.setActive(enable);
        this.entityDamageListener.setActive(enable || Config.enableDamageIndicator());
        if (this.attackStrengthListener != null) {
            this.attackStrengthListener.setActive(enable);
        }
    }

    @Override
    public void disable() {
        this.attributeEventListener.setActive(false);
        this.entityDamageListener.setActive(false);
        if (this.attackStrengthListener != null) {
            this.attackStrengthListener.setActive(false);
        }
    }

    @Override
    protected double vanillaEntityTypeDefaultBaseValue(Key entityType, Key attribute, double fallback) {
        Map<Key, Double> attributes = this.vanillaDefaultAttributes.get(entityType);
        if (attributes == null) return fallback;
        return attributes.getOrDefault(attribute, fallback);
    }

    private Map<Key, Map<Key, Double>> buildVanillaDefaultAttributeTable() {
        List<Map.Entry<Key, Object>> attributeHolders = new ArrayList<>();
        for (Object attributeId : RegistryProxy.INSTANCE.keySet(BuiltInRegistriesProxy.ATTRIBUTE)) {
            Object holder = VersionHelper.isOrAbove1_20_5
                    ? RegistryUtils.getHolderById(BuiltInRegistriesProxy.ATTRIBUTE, attributeId)
                    : RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.ATTRIBUTE, attributeId);
            if (holder != null) {
                attributeHolders.add(Map.entry(KeyUtils.identifierToKey(attributeId), holder));
            }
        }
        Map<Key, Map<Key, Double>> table = new HashMap<>();
        for (Object typeId : RegistryProxy.INSTANCE.keySet(BuiltInRegistriesProxy.ENTITY_TYPE)) {
            Object nmsEntityType = RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.ENTITY_TYPE, typeId);
            if (nmsEntityType == null) continue;
            Object supplier = DefaultAttributesProxy.INSTANCE.getSupplier(nmsEntityType);
            if (supplier == null) continue;
            Map<Key, Double> attributes = new HashMap<>();
            for (Map.Entry<Key, Object> entry : attributeHolders) {
                boolean has = VersionHelper.isOrAbove1_20_5
                        ? AttributeSupplierProxy.INSTANCE.hasAttribute(supplier, entry.getValue())
                        : AttributeSupplierProxy.INSTANCE.hasAttribute$legacy(supplier, entry.getValue());
                if (has) {
                    double baseValue = VersionHelper.isOrAbove1_20_5
                            ? AttributeSupplierProxy.INSTANCE.getBaseValue(supplier, entry.getValue())
                            : AttributeSupplierProxy.INSTANCE.getBaseValue$legacy(supplier, entry.getValue());
                    attributes.put(entry.getKey(), baseValue);
                }
            }
            if (!attributes.isEmpty()) {
                table.put(KeyUtils.identifierToKey(typeId), Map.copyOf(attributes));
            }
        }
        return table;
    }
}
