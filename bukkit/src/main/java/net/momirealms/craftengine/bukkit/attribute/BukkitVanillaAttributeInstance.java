package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeModifier;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes.AttributeInstanceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes.AttributeModifierProxy;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class BukkitVanillaAttributeInstance implements VanillaAttributeInstance {
    private final Object instance;

    public BukkitVanillaAttributeInstance(Object instance) {
        this.instance = instance;
    }

    @Override
    public double getValue() {
        return AttributeInstanceProxy.INSTANCE.getValue(this.instance);
    }

    @Override
    public double getBaseValue() {
        return AttributeInstanceProxy.INSTANCE.getBaseValue(this.instance);
    }

    @Override
    public void setBaseValue(double baseValue) {
        AttributeInstanceProxy.INSTANCE.setBaseValue(this.instance, baseValue);
    }

    @Override
    public void addOrUpdateTransientModifier(Key id, VanillaAttributeModifier.Operation operation, double amount) {
        if (VersionHelper.isOrAbove1_21) {
            Object modifier = AttributeModifierProxy.INSTANCE.newInstance(KeyUtils.toIdentifier(id), amount, toProxyOperation(operation));
            AttributeInstanceProxy.INSTANCE.addOrUpdateTransientModifier(this.instance, modifier);
        } else {
            // 1.20.6-：modifier 以 UUID 为键且无 addOrUpdateTransientModifier，先删后加
            UUID uuid = legacyUUID(id);
            AttributeInstanceProxy.INSTANCE.removeModifier$legacy(this.instance, uuid);
            Object modifier = AttributeModifierProxy.INSTANCE.newInstance(uuid, id.asString(), amount, toProxyOperation(operation));
            AttributeInstanceProxy.INSTANCE.addTransientModifier(this.instance, modifier);
        }
    }

    @Override
    public void removeModifier(Key id) {
        if (VersionHelper.isOrAbove1_21) {
            AttributeInstanceProxy.INSTANCE.removeModifier(this.instance, KeyUtils.toIdentifier(id));
        } else {
            AttributeInstanceProxy.INSTANCE.removeModifier$legacy(this.instance, legacyUUID(id));
        }
    }

    private static UUID legacyUUID(Key id) {
        return UUID.nameUUIDFromBytes(id.asString().getBytes(StandardCharsets.UTF_8));
    }

    private static Object toProxyOperation(VanillaAttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> AttributeModifierProxy.OperationProxy.ADD_VALUE;
            case ADD_MULTIPLIED_BASE -> AttributeModifierProxy.OperationProxy.ADD_MULTIPLIED_BASE;
            case ADD_MULTIPLIED_TOTAL -> AttributeModifierProxy.OperationProxy.ADD_MULTIPLIED_TOTAL;
        };
    }
}
