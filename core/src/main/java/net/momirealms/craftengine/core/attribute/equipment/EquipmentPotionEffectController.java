package net.momirealms.craftengine.core.attribute.equipment;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class EquipmentPotionEffectController {
    public static final int LEASE_TICKS = 600;
    private static final int REFRESH_THRESHOLD = 300;

    private final LivingEntity entity;
    private final EquipmentPotionEffectStateStore store;
    private final Map<Key, RuntimeState> states = new Object2ObjectArrayMap<>(8);
    private Map<Key, SetPotionEffect> desired = Map.of();
    private int currentTick;
    private boolean mutating;

    public EquipmentPotionEffectController(LivingEntity entity) {
        this.entity = entity;
        this.store = new EquipmentPotionEffectStateStore(entity);
        for (Map.Entry<Key, ManagedPotionEffectState> entry : this.store.load().entrySet()) {
            ManagedPotionEffectState persisted = entry.getValue();
            this.states.put(entry.getKey(), new RuntimeState(
                    persisted.managed(),
                    persisted.shadow(),
                    0
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
        reconcileAll();
    }

    public void tick() {
        this.currentTick++;
    }

    public void close(boolean death) {
        this.desired = Map.of();
        if (death) {
            this.states.clear();
            persist();
            return;
        }
        reconcileAll();
    }

    public void observeExternalEffect(PotionEffectSnapshot effect) {
        if (this.mutating) return;
        RuntimeState state = this.states.get(effect.type());
        if (state != null) {
            PotionEffectSnapshot currentShadow = remainingShadow(state);
            PotionEffectSnapshot updatedShadow = PotionEffectSnapshot.getBetterEffect(currentShadow, effect);
            if (Objects.equals(currentShadow, updatedShadow)) return;
            state.setShadow(updatedShadow, this.currentTick);
            persist();
        }
    }

    public void observeExternalClear(Key type) {
        if (this.mutating) return;
        RuntimeState state = this.states.get(type);
        if (state != null && state.shadow != null) {
            state.setShadow(null, this.currentTick);
            persist();
        }
    }

    public boolean isMutating() {
        return this.mutating;
    }

    private void reconcileAll() {
        Set<Key> types = new HashSet<>(this.states.keySet());
        types.addAll(this.desired.keySet());
        for (Key type : types) {
            reconcile(type);
        }
    }

    private void reconcile(Key type) {
        SetPotionEffect wanted = this.desired.get(type);
        RuntimeState state = this.states.get(type);
        PotionEffectSnapshot actual = this.entity.getPotionEffect(type);
        boolean managedVisible = state != null && matchesManaged(actual, state.managed);

        if (wanted == null) {
            if (state == null) return;
            restoreAndForget(type, state, actual, managedVisible);
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

        PotionEffectSnapshot shadow = state == null ? null : remainingShadow(state);
        PotionEffectSnapshot external = state == null ? actual
                : PotionEffectSnapshot.getBetterEffect(shadow, actual);
        if (external != null && external.amplifier() >= wanted.amplifier()) {
            if (state != null) {
                if (!Objects.equals(shadow, external) || !wanted.sameEffect(state.managed)) {
                    state.setShadow(external, this.currentTick);
                    state.managed = wanted;
                    persist();
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
                    this.currentTick
            );
            this.states.put(type, state);
        } else {
            if (actual != null) {
                state.setShadow(PotionEffectSnapshot.getBetterEffect(remainingShadow(state), actual), this.currentTick);
            }
            state.managed = wanted;
        }
        installManaged(wanted, state, actual);
    }

    private void installManaged(SetPotionEffect wanted, RuntimeState state, @Nullable PotionEffectSnapshot actual) {
        state.managed = wanted;
        persist();
        if (actual != null) {
            remove(wanted.type());
        }
        apply(wanted, LEASE_TICKS);
    }

    private void restoreAndForget(Key type, RuntimeState state, @Nullable PotionEffectSnapshot actual, boolean managedVisible) {
        if (managedVisible) {
            remove(type);
            actual = this.entity.getPotionEffect(type);
        }
        PotionEffectSnapshot shadow = remainingShadow(state);
        if (shadow != null && !shadow.isExpired()) {
            if (actual == null || actual.amplifier() <= shadow.amplifier()) {
                apply(shadow);
            }
        }
        this.states.remove(type);
        persist();
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
    private PotionEffectSnapshot remainingShadow(RuntimeState state) {
        PotionEffectSnapshot shadow = state.shadow;
        if (shadow == null || shadow.isInfinite()) return shadow;
        int elapsed = Math.max(0, this.currentTick - state.shadowAnchorTick);
        return shadow.withDuration(Math.max(0, shadow.duration() - elapsed));
    }

    private void persist() {
        if (this.states.isEmpty()) {
            this.store.save(Map.of());
            return;
        }
        Map<Key, ManagedPotionEffectState> persisted = new HashMap<>(this.states.size());
        for (Map.Entry<Key, RuntimeState> entry : this.states.entrySet()) {
            RuntimeState state = entry.getValue();
            PotionEffectSnapshot shadow = remainingShadow(state);
            state.setShadow(shadow, this.currentTick);
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
        private int shadowAnchorTick;

        private RuntimeState(
                SetPotionEffect managed,
                @Nullable PotionEffectSnapshot shadow,
                int shadowAnchorTick
        ) {
            this.managed = managed;
            this.shadow = shadow;
            this.shadowAnchorTick = shadowAnchorTick;
        }

        private void setShadow(@Nullable PotionEffectSnapshot shadow, int anchorTick) {
            this.shadow = shadow;
            this.shadowAnchorTick = anchorTick;
        }
    }
}
