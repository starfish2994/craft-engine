package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.context.AbstractChainParameterContext;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;

public class EntityDamageContext extends AbstractChainParameterContext {
    private final DamageEvent event;
    private final double originalDamage;

    public EntityDamageContext(DamageEvent event, @NotNull ContextHolder contexts) {
        super(contexts);
        this.event = event;
        this.originalDamage = event.damage();
    }

    @NotNull
    public static EntityDamageContext of(DamageEvent event, @NotNull ContextHolder.Builder contexts) {
        Entity entity = event.source().causingEntity();
        if (entity != null) {
            contexts.withParameter(DirectContextParameters.CAUSING_ENTITY, entity);
        }
        contexts.withParameter(DirectContextParameters.ENTITY, event.victim());
        contexts.withParameter(DirectContextParameters.ORIGINAL_DAMAGE, event.damage());
        contexts.withParameter(DirectContextParameters.IS_CRITICAL, event.source().isCritical());
        contexts.withParameter(DirectContextParameters.IS_SWEEP, event.isSweepAttack());
        contexts.withParameter(DirectContextParameters.IS_ATTACK_READY, event.isAttackReady());
        contexts.withParameter(DirectContextParameters.ATTACK_STRENGTH, event.attackStrength());
        return new EntityDamageContext(event, contexts.build());
    }

    public DamageEvent event() {
        return this.event;
    }

    public double originalDamage() {
        return this.originalDamage;
    }
}
