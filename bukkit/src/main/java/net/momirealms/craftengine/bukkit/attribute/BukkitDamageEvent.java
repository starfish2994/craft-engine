package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.attribute.*;
import net.momirealms.craftengine.core.attribute.formula.DamageEvent;
import net.momirealms.craftengine.core.attribute.formula.DamageSource;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.setting.value.AttributeModifiers;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.damage.CraftDamageSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.projectile.AbstractArrowProxy;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BukkitDamageEvent implements DamageEvent {
    private final EntityDamageEvent event;
    private final BukkitDamageSource source;
    private final BukkitAttributeManager manager;
    private final Entity victim;
    private final AttributeGetter victimAttributes;
    private final AttributeGetter attackerAttributes;
    private final Context attackerContext;
    // 本次攻击实际使用的武器及其合并修饰符，惰性解析（null 表示无）
    private boolean weaponResolved;
    @Nullable
    private Item activeWeapon;
    @Nullable
    private List<SlotAttributeModifierConfig> activeWeaponModifiers;

    public BukkitDamageEvent(BukkitAttributeManager manager, EntityDamageEvent event) {
        this.manager = manager;
        this.event = event;
        this.source = new BukkitDamageSource(CraftDamageSourceProxy.INSTANCE.getHandle(event.getDamageSource()));
        org.bukkit.entity.Entity victimEntity = this.event.getEntity();
        this.victim = BukkitAdaptor.adapt(victimEntity);
        AttributeContainer victimContainer = manager.getContainer(victimEntity.getUniqueId());
        this.victimAttributes = victimContainer == null ? EmptyAttributeHolder.INSTANCE : victimContainer;
        this.attackerAttributes = causingEntityAttributes();
        Entity causingEntity = this.source.causingEntity();
        this.attackerContext = causingEntity instanceof Player player ? PlayerOptionalContext.of(player) : PlayerOptionalContext.emptyImmutable();
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

    @SuppressWarnings("deprecation")
    public AttributeGetter causingEntityAttributes() {
        org.bukkit.entity.Entity entity = this.source.causingBukkitEntity();
        if (entity == null) {
            return EmptyAttributeHolder.INSTANCE;
        }
        AttributeGetter container = this.manager.getContainer(entity.getUniqueId());
        if (container == null) {
            List<MetadataValue> attribute = entity.getMetadata(AttributeManager.META_KEY);
            if (!attribute.isEmpty()) {
                MetadataValue first = attribute.getFirst();
                container = (AttributeGetter) first.value();
            }
        }
        return container == null ? EmptyAttributeHolder.INSTANCE : container;
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
        Item weapon = activeWeapon();
        if (weapon == null) return 0;
        if (this.activeWeaponModifiers == null) {
            this.activeWeaponModifiers = this.manager.getItemAttributeModifiers(weapon);
        }
        return AttributeModifiers.weaponValue(this.activeWeaponModifiers, attribute, this.attackerContext);
    }

    @Nullable
    private Item activeWeapon() {
        if (!this.weaponResolved) {
            this.weaponResolved = true;
            this.activeWeapon = resolveActiveWeapon();
        }
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
        // 弹射物：原版记录的发射武器（弓/弩/三叉戟），1.21.2 起可用
        if (VersionHelper.isOrAbove1_21_2) {
            Object direct = this.source.directNmsEntity();
            if (direct != null && AbstractArrowProxy.CLASS.isInstance(direct)) {
                Object weaponStack = AbstractArrowProxy.INSTANCE.getWeaponItem(direct);
                if (weaponStack != null) {
                    return ItemStackUtils.wrap(weaponStack);
                }
            }
        }
        return null;
    }
}
