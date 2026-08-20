package net.momirealms.craftengine.core.entity.tick;

import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.Arrays;

public final class EntityTickScheduler {
    public static final long NEVER = Long.MAX_VALUE;
    private static final int WHEEL_BITS = 8;  // 256 ticks
    private static final int WHEEL_SIZE = 1 << WHEEL_BITS;
    private static final int WHEEL_MASK = WHEEL_SIZE - 1;
    private static final int BUCKET_INITIAL_CAPACITY = 64;

    private final WheelBucket[] wheel = new WheelBucket[WHEEL_SIZE];
    private int scheduledTaskCount;
    private long currentTick;

    public EntityTickScheduler() {
        for (int i = 0; i < this.wheel.length; i++) {
            this.wheel[i] = new WheelBucket();
        }
    }

    @FunctionalInterface
    public interface Task {

        long runDue(long currentTick);
    }

    public long currentTick() {
        return this.currentTick;
    }

    public Registration register(Task task) {
        return new Registration(this, task);
    }

    public void advance() {
        long now = ++this.currentTick;
        WheelBucket bucket = this.wheel[slot(now)];

        RegistrationList batch = bucket.beginDrain();
        Registration registration;
        while ((registration = batch.removeLast()) != null) {
            this.scheduledTaskCount--;

            if (registration.remainingRounds > 0) {
                registration.remainingRounds--;
                addToBucket(bucket, registration);
                continue;
            }

            registration.running = true;
            registration.deadline = NEVER;
            long next = NEVER;
            try {
                next = registration.task.runDue(now);
            } catch (Throwable throwable) {
                // A broken component must not prevent other due entities from ticking.
                CraftEngine.instance().logger().error("Failed to run scheduled entity work", throwable);
            } finally {
                registration.running = false;
            }

            if (registration.closed) continue;
            if (registration.sleepRequested) {
                next = NEVER;
            }
            if (registration.pendingDeadline != NEVER) {
                next = Math.min(next, registration.pendingDeadline);
            }
            registration.pendingDeadline = NEVER;
            registration.sleepRequested = false;
            reschedule(registration, next);
        }
    }

    public int scheduledTaskCount() {
        return this.scheduledTaskCount;
    }

    private int slot(long deadline) {
        return (int) (deadline & WHEEL_MASK);
    }

    private long normalizeDeadline(long deadline) {
        if (deadline == NEVER) return NEVER;
        return Math.max(deadline, this.currentTick + 1);
    }

    private void reschedule(Registration registration, long deadline) {
        if (registration.closed) return;
        deadline = normalizeDeadline(deadline);
        if (deadline == NEVER) {
            removeScheduled(registration);
            registration.deadline = NEVER;
            registration.remainingRounds = 0;
            return;
        }
        if (registration.owner != null && registration.deadline == deadline) {
            return;
        }

        removeScheduled(registration);
        long delay = deadline - this.currentTick;
        registration.deadline = deadline;
        registration.remainingRounds = (delay - 1) >>> WHEEL_BITS;
        addToBucket(this.wheel[slot(deadline)], registration);
    }

    private void addToBucket(WheelBucket bucket, Registration registration) {
        bucket.add(registration);
        this.scheduledTaskCount++;
    }

    private void removeScheduled(Registration registration) {
        RegistrationList owner = registration.owner;
        if (owner == null) return;
        owner.remove(registration);
        this.scheduledTaskCount--;
    }

    private void wake(Registration registration) {
        if (registration.closed) return;
        long deadline = this.currentTick + 1;
        if (registration.running) {
            registration.sleepRequested = false;
            registration.pendingDeadline = Math.min(registration.pendingDeadline, deadline);
        } else if (registration.owner == null || registration.deadline > deadline) {
            reschedule(registration, deadline);
        }
    }

    private void scheduleAt(Registration registration, long deadline) {
        if (registration.closed) return;
        if (deadline == NEVER) {
            sleep(registration);
            return;
        }
        deadline = normalizeDeadline(deadline);
        if (registration.running) {
            registration.sleepRequested = false;
            registration.pendingDeadline = deadline;
        } else {
            reschedule(registration, deadline);
        }
    }

    private void sleep(Registration registration) {
        if (registration.closed) return;
        if (registration.running) {
            registration.pendingDeadline = NEVER;
            registration.sleepRequested = true;
        } else {
            removeScheduled(registration);
            registration.deadline = NEVER;
            registration.remainingRounds = 0;
        }
    }

    private void close(Registration registration) {
        if (registration.closed) return;
        registration.closed = true;
        registration.pendingDeadline = NEVER;
        registration.sleepRequested = true;
        removeScheduled(registration);
        registration.deadline = NEVER;
        registration.remainingRounds = 0;
    }

    public static final class Registration {
        private final EntityTickScheduler scheduler;
        private final Task task;
        private RegistrationList owner;
        private int listIndex = -1;
        private long deadline = NEVER;
        private long remainingRounds;
        private long pendingDeadline = NEVER;
        private boolean running;
        private boolean sleepRequested;
        private boolean closed;

        private Registration(EntityTickScheduler scheduler, Task task) {
            this.scheduler = scheduler;
            this.task = task;
        }

        public void wake() {
            this.scheduler.wake(this);
        }

        public void scheduleAt(long absoluteTick) {
            this.scheduler.scheduleAt(this, absoluteTick);
        }

        public void sleep() {
            this.scheduler.sleep(this);
        }

        public boolean scheduled() {
            return this.owner != null || this.running;
        }

        public long scheduledTick() {
            return this.deadline;
        }

        public void close() {
            this.scheduler.close(this);
        }
    }

    private static final class WheelBucket {
        private RegistrationList active = new RegistrationList();
        private RegistrationList standby = new RegistrationList();

        private RegistrationList beginDrain() {
            RegistrationList batch = this.active;
            this.active = this.standby;
            this.standby = batch;
            return batch;
        }

        private void add(Registration registration) {
            this.active.add(registration);
        }
    }

    private static final class RegistrationList {
        private Registration[] elements = new Registration[BUCKET_INITIAL_CAPACITY];
        private int size;

        private void add(Registration registration) {
            if (registration.owner != null) {
                throw new IllegalStateException("Registration is already scheduled");
            }
            ensureCapacity(this.size + 1);
            registration.owner = this;
            registration.listIndex = this.size;
            this.elements[this.size++] = registration;
        }

        private void remove(Registration registration) {
            if (registration.owner != this) {
                throw new IllegalStateException("Registration belongs to another bucket");
            }
            removeAt(registration.listIndex);
        }

        private Registration removeLast() {
            return this.size == 0 ? null : removeAt(this.size - 1);
        }

        private Registration removeAt(int index) {
            Registration removed = this.elements[index];
            int last = --this.size;
            Registration moved = this.elements[last];
            this.elements[last] = null;
            if (index != last) {
                this.elements[index] = moved;
                moved.listIndex = index;
            }
            removed.owner = null;
            removed.listIndex = -1;
            return removed;
        }

        private void ensureCapacity(int capacity) {
            if (capacity <= this.elements.length) return;
            int length = this.elements.length << 1;
            this.elements = Arrays.copyOf(this.elements, Math.max(length, capacity));
        }
    }
}
