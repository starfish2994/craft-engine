package net.momirealms.craftengine.bukkit.attribute.damage;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.attribute.AttributeEventListener;
import net.momirealms.craftengine.bukkit.attribute.BukkitAttributeManager;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.attribute.*;
import net.momirealms.craftengine.core.attribute.damage.DamageEvent;
import net.momirealms.craftengine.core.attribute.damage.DamageSource;
import net.momirealms.craftengine.core.attribute.damage.EntityDamageContext;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.setting.value.AttributeModifiers;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.damage.CraftDamageSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.projectile.AbstractArrowProxy;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BukkitDamageEvent implements DamageEvent {
    private final EntityDamageEvent event;
    private final BukkitDamageSource source;
    private final BukkitAttributeManager manager;
    private final Entity victim;
    private final AttributeGetter victimAttributes;
    private final AttributeGetter attackerAttributes;
    private final EntityDamageContext context;
    private final Item activeWeapon;
    private final Map<String, Double> damageParts = new LinkedHashMap<>();
    @Nullable
    private List<SlotAttributeModifierConfig> activeWeaponModifiers;

    public BukkitDamageEvent(BukkitAttributeManager manager, EntityDamageEvent event) {
        this.manager = manager;
        this.event = event;
        this.source = new BukkitDamageSource(CraftDamageSourceProxy.INSTANCE.getHandle(event.getDamageSource()));
        org.bukkit.entity.Entity victimEntity = this.event.getEntity();
        this.victim = BukkitAdaptor.adapt(victimEntity);
        LivingEntityHolder victimHolder = CraftEngine.instance().entityManager().getEntityHolder(victimEntity.getUniqueId());
        this.victimAttributes = victimHolder == null ? EmptyAttributeHolder.INSTANCE : victimHolder.attributes();
        this.attackerAttributes = causingEntityAttributes();
        Item weapon = this.resolveActiveWeapon();
        this.activeWeapon = weapon == null || weapon.isEmpty() ? null : weapon;
        this.context = EntityDamageContext.of(this, ContextHolder.builder().withOptionalParameter(DirectContextParameters.ITEM, this.activeWeapon));
    }

    @Override
    public EntityDamageContext context() {
        return this.context;
    }

    @Override
    public double damage() {
        return this.event.getDamage();
    }

    @Override
    public void setDamage(double damage) {
        this.event.setDamage(damage);
    }

    @Override
    public DamageSource source() {
        return this.source;
    }

    @Override
    public Entity victim() {
        return this.victim;
    }

    @Override
    public boolean isSweepAttack() {
        return this.event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
    }

    @Override
    public void recordDamagePart(String id, double amount) {
        this.damageParts.put(id, amount);
        this.context.contexts().withParameter(ContextKey.direct("damage_" + id), amount);
    }

    public void initFinalDamage() {
        this.context.contexts().withParameter(DirectContextParameters.DAMAGE, this.event.getFinalDamage());
    }

    @Override
    public Map<String, Double> damageParts() {
        return this.damageParts;
    }

    @SuppressWarnings("deprecation")
    public AttributeGetter causingEntityAttributes() {
        org.bukkit.entity.Entity entity = this.source.causingBukkitEntity();
        if (entity == null) {
            return EmptyAttributeHolder.INSTANCE;
        }
        LivingEntityHolder holder = CraftEngine.instance().entityManager().getEntityHolder(entity.getUniqueId());
        AttributeGetter attributes = holder == null ? null : holder.attributes();
        if (attributes == null) {
            List<MetadataValue> attribute = entity.getMetadata(AttributeManager.META_KEY);
            if (!attribute.isEmpty()) {
                MetadataValue first = attribute.getFirst();
                attributes = (AttributeGetter) first.value();
            }
        }
        return attributes == null ? EmptyAttributeHolder.INSTANCE : attributes;
    }

    @Override
    public double getAttributeValue(AttributeSide side, Attribute attribute) {
        if (attribute.derived() != null) {
            return attribute.derive(a -> getAttributeValue(side, a));
        }
        if (side == AttributeSide.ATTACKER) {
            return this.attackerAttributes.getAttributeValue(attribute) + weaponAttributeValue(attribute);
        } else {
            return this.victimAttributes.getAttributeValue(attribute);
        }
    }

    private double weaponAttributeValue(Attribute attribute) {
        Item weapon = this.activeWeapon;
        if (weapon == null || weapon.isEmpty()) return 0;
        if (this.activeWeaponModifiers == null) {
            this.activeWeaponModifiers = this.manager.getItemAttributeModifiers(weapon);
        }
        return AttributeModifiers.weaponValue(this.activeWeaponModifiers, attribute, this.context);
    }

    @Nullable
    @Override
    public Item activeWeapon() {
        return this.activeWeapon;
    }

    @Nullable
    private Item resolveActiveWeapon() {
        // 近战等直接伤害：攻击者主手物品
        if (this.source.isDirect()) {
            if (this.source.causingEntity() instanceof LivingEntity living) {
                return living.getItemInHand(InteractionHand.MAIN_HAND);
            }
            return null;
        }
        // 弹射物的武器
        Object direct = this.source.directNmsEntity();
        if (AbstractArrowProxy.CLASS.isInstance(direct)) {
            if (VersionHelper.isOrAbove1_21) {
                Object weaponStack = AbstractArrowProxy.INSTANCE.getWeaponItem(direct);
                if (weaponStack != null) {
                    return ItemStackUtils.wrap(weaponStack);
                }
            } else {
                @SuppressWarnings("deprecation")
                List<MetadataValue> metadata = EntityProxy.INSTANCE.getBukkitEntity(direct).getMetadata(AttributeEventListener.PROJECTILE_WEAPON);
                if (!metadata.isEmpty()) {
                    return ItemStackUtils.wrap(metadata.getFirst());
                }
            }
        }
        return null;
    }
}
