package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LivingEntity extends Entity {

    @Nullable
    VanillaAttributeInstance getVanillaAttribute(Key attribute);

    double health();

    void setHealth(double amount);

    double maxHealth();

    default void heal(double amount) {
        double targetHealth = Math.min(this.health() + amount, this.maxHealth());
        targetHealth = Math.max(targetHealth, health());
        this.setHealth(targetHealth);
    }

    double luck();

    void damage(double amount, Key damageType, @Nullable Entity causeEntity);

    void addPotionEffect(Key potionEffectType, int duration, int amplifier, boolean ambient, boolean particles, boolean showIcon);

    @Nullable
    PotionEffectSnapshot getPotionEffect(Key potionEffectType);

    void removePotionEffect(Key potionEffectType);

    void clearPotionEffects();

    @NotNull
    Item getItemInHand(InteractionHand hand);

    void setItemInHand(InteractionHand hand, Item item);

    @NotNull
    Item getItemByEquipmentSlot(EquipmentSlot slot);

    boolean isSneaking();

    boolean isSwimming();

    boolean isClimbing();

    boolean isGliding();
}
