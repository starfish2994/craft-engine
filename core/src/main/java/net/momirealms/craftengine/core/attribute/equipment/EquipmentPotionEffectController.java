package net.momirealms.craftengine.core.attribute.equipment;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.entity.tick.EntityTickScheduler;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class EquipmentPotionEffectController {
    public static final int LEASE_TICKS = 600;
    private static final int REFRESH_THRESHOLD = 300;
    private static final int AUDIT_INTERVAL = 20;

    private final LivingEntityHolder holder;
    private final LivingEntity entity;
    private final EquipmentPotionEffectStateStore store;
    private final Map<Key, RuntimeState> states = new Object2ObjectArrayMap<>(8);
    private Map<Key, SetPotionEffect> desired = Map.of();
    private boolean mutating;

    public EquipmentPotionEffectController(LivingEntityHolder holder) {
        this.holder = holder;
        this.entity = holder.entity;
        this.store = new EquipmentPotionEffectStateStore(entity);
        for (Map.Entry<Key, ManagedPotionEffectState> entry : this.store.load().entrySet()) {
            ManagedPotionEffectState persisted = entry.getValue();
            this.states.put(entry.getKey(), new RuntimeState(
                    persisted.managed(),
                    persisted.shadow(),
                    holder.currentTick()
            ));
        }
    }

    public void update(Collection<SetPotionEffect> effects) {
        Map<Key, SetPotionEffect> highest = new HashMap<>();
        for (SetPotionEffect effect : effects) {
            SetPotionEffect previous = highest.get(effect.type());
            if (previous == null || effect.amplifier() > previous.amplifier()) {
                highest.put(effect.type(), effect);
            }
        }
        this.desired = highest.isEmpty() ? Map.of() : highest;
        long now = this.holder.currentTick();
        reconcileAll(now);
        this.holder.refreshPotionEffectSchedule();
    }

    /** Runs only while this controller has an equipment claim or recovery state. */
    public long runDue(long currentTick) {
        reconcileAll(currentTick);
        return nextRequiredTick(currentTick);
    }

    public long nextRequiredTick(long currentTick) {
        // Paper events are wake-up hints, not a complete mutation log. Keep the
        // fallback audit, but only while this entity actually has a claim/state.
        return !this.holder.periodicWorkEnabled() || this.desired.isEmpty() && this.states.isEmpty()
                ? EntityTickScheduler.NEVER
                : currentTick + AUDIT_INTERVAL;
    }

    public void close(boolean death) {
        this.desired = Map.of();
        if (death) {
            this.states.clear();
            persist(this.holder.currentTick());
            return;
        }
        reconcileAll(this.holder.currentTick());
    }

    public void observeExternalEffect(PotionEffectSnapshot effect) {
        if (this.mutating) return;
        long now = this.holder.currentTick();
        RuntimeState state = this.states.get(effect.type());
        if (state != null) {
            PotionEffectSnapshot currentShadow = remainingShadow(state, now);
            PotionEffectSnapshot updatedShadow = PotionEffectSnapshot.getBetterEffect(currentShadow, effect);
            if (!Objects.equals(currentShadow, updatedShadow)) {
                state.setShadow(updatedShadow, now);
                persist(now);
            }
            // Paper fires the event before committing activeEffects. Reconcile next tick.
            this.holder.wakePotionEffects();
        }
    }

    public void observeExternalClear(Key type) {
        if (this.mutating) return;
        RuntimeState state = this.states.get(type);
        if (state != null) {
            long now = this.holder.currentTick();
            if (state.shadow != null) {
                state.setShadow(null, now);
                persist(now);
            }
            this.holder.wakePotionEffects();
        }
    }

    public boolean isMutating() {
        return this.mutating;
    }

    private void reconcileAll(long currentTick) {
        Set<Key> types = new HashSet<>(this.states.keySet());
        types.addAll(this.desired.keySet());
        for (Key type : types) {
            reconcile(type, currentTick);
        }
    }

    private void reconcile(Key type, long currentTick) {
        SetPotionEffect wanted = this.desired.get(type);
        RuntimeState state = this.states.get(type);
        PotionEffectSnapshot actual = this.entity.getPotionEffect(type);
        boolean managedVisible = state != null && matchesManaged(actual, state.managed);

        if (wanted == null) {
            if (state == null) return;
            restoreAndForget(type, state, actual, managedVisible, currentTick);
            return;
        }

        if (managedVisible) {
            if (!wanted.sameEffect(state.managed)) {
                remove(type);
                actual = this.entity.getPotionEffect(type);
            } else if (actual.duration() >= 0 && actual.duration() <= REFRESH_THRESHOLD) {
                apply(wanted, LEASE_TICKS);
                return;
            } else {
                return;
            }
        }

        PotionEffectSnapshot shadow = state == null ? null : remainingShadow(state, currentTick);
        PotionEffectSnapshot external = state == null ? actual
                : PotionEffectSnapshot.getBetterEffect(shadow, actual);
        if (external != null && external.amplifier() >= wanted.amplifier()) {
            if (state != null) {
                if (!Objects.equals(shadow, external) || !wanted.sameEffect(state.managed)) {
                    state.setShadow(external, currentTick);
                    state.managed = wanted;
                    persist(currentTick);
                }
                if (!external.isSameEffect(actual)) {
                    apply(external);
                }
            }
            return;
        }

        if (state == null) {
            state = new RuntimeState(
                    wanted,
                    actual,
                    currentTick
            );
            this.states.put(type, state);
        } else {
            if (actual != null) {
                state.setShadow(PotionEffectSnapshot.getBetterEffect(remainingShadow(state, currentTick), actual), currentTick);
            }
            state.managed = wanted;
        }
        installManaged(wanted, state, actual, currentTick);
    }

    private void installManaged(SetPotionEffect wanted, RuntimeState state, @Nullable PotionEffectSnapshot actual, long currentTick) {
        state.managed = wanted;
        persist(currentTick);
        if (actual != null) {
            remove(wanted.type());
        }
        apply(wanted, LEASE_TICKS);
    }

    private void restoreAndForget(Key type, RuntimeState state, @Nullable PotionEffectSnapshot actual, boolean managedVisible, long currentTick) {
        if (managedVisible) {
            remove(type);
            actual = this.entity.getPotionEffect(type);
        }
        PotionEffectSnapshot shadow = remainingShadow(state, currentTick);
        if (shadow != null && !shadow.isExpired()) {
            if (actual == null || actual.amplifier() <= shadow.amplifier()) {
                apply(shadow);
            }
        }
        this.states.remove(type);
        persist(currentTick);
    }

    private void apply(SetPotionEffect effect, int duration) {
        mutate(() -> this.entity.addPotionEffect(
                effect.type(),
                duration,
                effect.amplifier(),
                effect.ambient(),
                effect.particles(),
                effect.icon()
        ));
    }

    private void apply(PotionEffectSnapshot effect) {
        mutate(() -> this.entity.addPotionEffect(
                effect.type(),
                effect.duration(),
                effect.amplifier(),
                effect.ambient(),
                effect.particles(),
                effect.showIcon()
        ));
    }

    private void remove(Key type) {
        mutate(() -> this.entity.removePotionEffect(type));
    }

    private void mutate(Runnable action) {
        this.mutating = true;
        try {
            action.run();
        } finally {
            this.mutating = false;
        }
    }

    private boolean matchesManaged(@Nullable PotionEffectSnapshot actual, SetPotionEffect managed) {
        // MobEffectInstance has no CraftEngine source marker. This fingerprint plus
        // the finite lease window is therefore our best-effort ownership check.
        if (actual == null || !actual.type().equals(managed.type())) return false;
        if (actual.amplifier() != managed.amplifier()
                || actual.ambient() != managed.ambient()
                || actual.particles() != managed.particles()
                || actual.showIcon() != managed.icon()) {
            return false;
        }
        return actual.duration() >= 0 && actual.duration() <= LEASE_TICKS;
    }

    @Nullable
    private PotionEffectSnapshot remainingShadow(RuntimeState state, long currentTick) {
        PotionEffectSnapshot shadow = state.shadow;
        if (shadow == null || shadow.isInfinite()) return shadow;
        long elapsed = Math.max(0, currentTick - state.shadowAnchorTick);
        return shadow.withDuration((int) Math.max(0, shadow.duration() - elapsed));
    }

    private void persist(long currentTick) {
        if (this.states.isEmpty()) {
            this.store.save(Map.of());
            return;
        }
        Map<Key, ManagedPotionEffectState> persisted = new HashMap<>(this.states.size());
        for (Map.Entry<Key, RuntimeState> entry : this.states.entrySet()) {
            RuntimeState state = entry.getValue();
            PotionEffectSnapshot shadow = remainingShadow(state, currentTick);
            state.setShadow(shadow, currentTick);
            persisted.put(entry.getKey(), new ManagedPotionEffectState(
                    state.managed,
                    shadow
            ));
        }
        this.store.save(persisted);
    }

    private static final class RuntimeState {
        private SetPotionEffect managed;
        @Nullable
        private PotionEffectSnapshot shadow;
        private long shadowAnchorTick;

        private RuntimeState(
                SetPotionEffect managed,
                @Nullable PotionEffectSnapshot shadow,
                long shadowAnchorTick
        ) {
            this.managed = managed;
            this.shadow = shadow;
            this.shadowAnchorTick = shadowAnchorTick;
        }

        private void setShadow(@Nullable PotionEffectSnapshot shadow, long anchorTick) {
            this.shadow = shadow;
            this.shadowAnchorTick = anchorTick;
        }
    }
}
