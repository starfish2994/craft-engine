package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDamageContext extends PlayerOptionalContext {
    private final DamageEvent event;
    private final double originalDamage;

    public EntityDamageContext(DamageEvent event, @Nullable Player player, @NotNull ContextHolder contexts) {
        super(player, contexts);
        this.event = event;
        this.originalDamage = event.damage();
    }

    @NotNull
    public static EntityDamageContext of(DamageEvent event, @NotNull ContextHolder.Builder contexts) {
        Player player = attackerOf(event);
        if (player != null) {
            contexts.withParameter(DirectContextParameters.PLAYER, player);
        }
        contexts.withParameter(DirectContextParameters.ORIGINAL_DAMAGE, event.damage());
        contexts.withParameter(DirectContextParameters.IS_CRITICAL, event.source().isCritical());
        contexts.withParameter(DirectContextParameters.IS_SWEEP, event.isSweepAttack());
        return new EntityDamageContext(event, player, contexts.build());
    }

    public DamageEvent event() {
        return this.event;
    }

    public double originalDamage() {
        return this.originalDamage;
    }

    @Nullable
    private static Player attackerOf(DamageEvent event) {
        return event.source().causingEntity() instanceof Player player ? player : null;
    }
}
