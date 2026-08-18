package net.momirealms.craftengine.core.attribute.equipment;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.entity.effect.PotionEffectSnapshot;
import net.momirealms.craftengine.core.entity.tick.EntityTickScheduler;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.plugin.context.Context;
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
    private final CandidateTracker candidates = new CandidateTracker();
    private long nextAuditTick = EntityTickScheduler.NEVER;
    private boolean reconcileRequested;
    private boolean mutating;

    public EquipmentPotionEffectController(LivingEntityHolder holder) {
        this.holder = holder;
        this.entity = holder.entity;
        this.store = new EquipmentPotionEffectStateStore(entity);
        for (Map.Entry<Key, PotionEffectSnapshot> entry : this.store.load().entrySet()) {
            this.states.put(entry.getKey(), new RuntimeState(
                    null,
                    entry.getValue(),
                    holder.currentTick()
            ));
        }
        scheduleAudit(holder.currentTick());
    }

    public void update(Collection<SetPotionEffect> effects) {
        long now = this.holder.currentTick();
        this.candidates.replace(effects, this.holder.context, now, this.entity.uuid().hashCode());
        reconcileAll(now);
        scheduleAudit(now);
        this.holder.refreshPotionEffectSchedule();
    }

    public long runDue(long currentTick) {
        boolean desiredChanged = this.candidates.runDue(this.holder.context, currentTick);
        boolean auditDue = currentTick >= this.nextAuditTick;
        boolean shouldReconcile = this.reconcileRequested || desiredChanged || auditDue;
        this.reconcileRequested = false;
        if (shouldReconcile) {
            reconcileAll(currentTick);
            scheduleAudit(currentTick);
        }
        return nextRequiredTick(currentTick);
    }

    public long nextRequiredTick(long currentTick) {
        if (!this.holder.periodicWorkEnabled()) return EntityTickScheduler.NEVER;
        return Math.min(this.candidates.nextTick(), this.nextAuditTick);
    }

    public void close(boolean death) {
        this.candidates.clear();
        this.nextAuditTick = EntityTickScheduler.NEVER;
        this.reconcileRequested = false;
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
            this.reconcileRequested = true;
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
            this.reconcileRequested = true;
            this.holder.wakePotionEffects();
        }
    }

    public boolean isMutating() {
        return this.mutating;
    }

    private void reconcileAll(long currentTick) {
        Set<Key> types = new HashSet<>(this.states.keySet());
        types.addAll(this.candidates.desired().keySet());
        for (Key type : types) {
            reconcile(type, currentTick);
        }
    }

    private void reconcile(Key type, long currentTick) {
        SetPotionEffect wanted = this.candidates.desired().get(type);
        RuntimeState state = this.states.get(type);
        PotionEffectSnapshot actual = this.entity.getPotionEffect(type);
        boolean managedVisible = state != null
                && state.managed != null
                && matchesManagedLease(
                        actual,
                        state.managed.type(),
                        state.managed.amplifier()
                );

        if (state != null && state.managed == null && actual != null && isPossibleManagedLease(actual)) {
            if (wanted != null && matchesManagedLease(
                    actual,
                    wanted.type(),
                    wanted.amplifier()
            )) {
                // After a restart the current equipment can adopt an identical residual lease.
                state.managed = wanted;
                managedVisible = true;
            } else {
                // No persisted fingerprint means this might be our old lease or an external
                // effect. Do not delete it; the finite lease will settle this within 30 seconds.
                return;
            }
        }

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
                state.managed = null;
                if (!Objects.equals(shadow, external)) {
                    state.setShadow(external, currentTick);
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

    static boolean matchesManagedLease(@Nullable PotionEffectSnapshot actual,
                                       Key type,
                                       int amplifier) {
        // Vanilla copies ambient/particle/icon flags from a lower, longer effect onto
        // the visible higher effect while keeping that lower effect hidden. Those
        // presentation fields therefore describe current NMS state, not ownership.
        // Type, amplifier and our bounded lease are the stable in-process fingerprint.
        if (actual == null || !actual.type().equals(type)) return false;
        return actual.amplifier() == amplifier && isPossibleManagedLease(actual);
    }

    private static boolean isPossibleManagedLease(PotionEffectSnapshot actual) {
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
        Map<Key, PotionEffectSnapshot> persisted = new HashMap<>(this.states.size());
        for (Map.Entry<Key, RuntimeState> entry : this.states.entrySet()) {
            RuntimeState state = entry.getValue();
            PotionEffectSnapshot shadow = remainingShadow(state, currentTick);
            state.setShadow(shadow, currentTick);
            if (shadow != null && !shadow.isExpired()) {
                persisted.put(entry.getKey(), shadow);
            }
        }
        this.store.save(persisted);
    }

    private void scheduleAudit(long currentTick) {
        this.nextAuditTick = this.candidates.desired().isEmpty() && this.states.isEmpty()
                ? EntityTickScheduler.NEVER
                : currentTick + AUDIT_INTERVAL;
    }

    static final class CandidateTracker {
        private List<CandidateState> states = List.of();
        private Map<Key, SetPotionEffect> desired = Map.of();
        private long nextTick = EntityTickScheduler.NEVER;

        void replace(Collection<SetPotionEffect> effects,
                     Context context,
                     long currentTick,
                     int phaseHash) {
            if (effects.isEmpty()) {
                clear();
                return;
            }
            List<CandidateState> tracked = new ArrayList<>(effects.size());
            long earliest = EntityTickScheduler.NEVER;
            for (SetPotionEffect effect : effects) {
                int interval = effect.updateInterval();
                boolean active = interval == 0 || effect.test(context);
                long deadline = interval == 0
                        ? EntityTickScheduler.NEVER
                        : currentTick + 1L + Math.floorMod(phaseHash, interval);
                tracked.add(new CandidateState(effect, active, deadline));
                earliest = Math.min(earliest, deadline);
            }
            this.states = tracked;
            this.nextTick = earliest;
            rebuildDesired();
        }

        boolean runDue(Context context, long currentTick) {
            if (currentTick < this.nextTick) return false;
            boolean activeChanged = false;
            long earliest = EntityTickScheduler.NEVER;
            for (CandidateState state : this.states) {
                int interval = state.effect.updateInterval();
                if (interval == 0) continue;
                if (currentTick >= state.nextTick) {
                    boolean active = state.effect.test(context);
                    if (active != state.active) {
                        state.active = active;
                        activeChanged = true;
                    }
                    state.nextTick = currentTick + interval;
                }
                earliest = Math.min(earliest, state.nextTick);
            }
            this.nextTick = earliest;
            return activeChanged && rebuildDesired();
        }

        Map<Key, SetPotionEffect> desired() {
            return this.desired;
        }

        long nextTick() {
            return this.nextTick;
        }

        void clear() {
            this.states = List.of();
            this.desired = Map.of();
            this.nextTick = EntityTickScheduler.NEVER;
        }

        private boolean rebuildDesired() {
            Map<Key, SetPotionEffect> highest = new Object2ObjectArrayMap<>(8);
            for (CandidateState state : this.states) {
                if (!state.active) continue;
                SetPotionEffect effect = state.effect;
                SetPotionEffect previous = highest.get(effect.type());
                if (previous == null || effect.amplifier() > previous.amplifier()) {
                    highest.put(effect.type(), effect);
                }
            }
            Map<Key, SetPotionEffect> updated = highest.isEmpty() ? Map.of() : highest;
            if (updated.equals(this.desired)) return false;
            this.desired = updated;
            return true;
        }
    }

    private static final class CandidateState {
        private final SetPotionEffect effect;
        private boolean active;
        private long nextTick;

        private CandidateState(SetPotionEffect effect, boolean active, long nextTick) {
            this.effect = effect;
            this.active = active;
            this.nextTick = nextTick;
        }
    }

    private static final class RuntimeState {
        @Nullable
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
