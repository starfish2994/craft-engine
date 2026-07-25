package net.momirealms.craftengine.bukkit.plugin.script;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventSubscriber;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BukkitScriptEventManager implements ScriptEventSubscriber {
    private final BukkitCraftEngine plugin;
    private final Map<Class<? extends Event>, EventGroup<?>> groups = new ConcurrentHashMap<>();

    public BukkitScriptEventManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public void subscribe(ScriptFile script, String eventClass, String function, @Nullable Map<String, Object> options) {
        Class<? extends Event> clazz = resolveEventClass(eventClass);
        if (clazz == null) {
            this.plugin.logger().warn("Script '" + script.id() + "' tried to subscribe unknown event class '" + eventClass + "'");
            return;
        }
        EventPriority priority = parsePriority(options);
        boolean ignoreCancelled = options == null || !Boolean.FALSE.equals(options.get("ignoreCancelled"));
        Subscription subscription = new Subscription(script, function, ignoreCancelled);
        EventGroup<?> group = this.groups.computeIfAbsent(clazz, EventGroup::new);
        if (!group.add(priority, subscription)) {
            return; // 重复订阅，忽略
        }
        script.onUnload(() -> removeSubscription(clazz, group, priority, subscription));
        // HandlerList 非线程安全，注册必须回到主线程
        this.plugin.scheduler().platform().run(() -> group.registerToBukkit(priority));
    }

    private void removeSubscription(Class<? extends Event> clazz, EventGroup<?> group, EventPriority priority, Subscription subscription) {
        group.remove(priority, subscription);
        if (group.isEmpty() && this.groups.remove(clazz, group)) {
            group.unregisterFromBukkit();
        }
    }

    private static EventPriority parsePriority(@Nullable Map<String, Object> options) {
        if (options == null) return EventPriority.NORMAL;
        Object priority = options.get("priority");
        if (priority == null) return EventPriority.NORMAL;
        try {
            return EventPriority.valueOf(priority.toString().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Class<? extends Event> resolveEventClass(String name) {
        Class<?> clazz = findClass(name);
        if (clazz != null && Event.class.isAssignableFrom(clazz)) {
            return (Class<? extends Event>) clazz;
        }
        return null;
    }

    @Nullable
    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private record Subscription(ScriptFile script, String function, boolean ignoreCancelled) {}

    private final class EventGroup<T extends Event> {
        private final Class<T> eventClass;
        private final Map<EventPriority, List<Subscription>> subscribers = new ConcurrentHashMap<>();
        private final Map<EventPriority, Listener> registeredListeners = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        EventGroup(Class<?> eventClass) {
            this.eventClass = (Class<T>) eventClass;
        }

        boolean add(EventPriority priority, Subscription subscription) {
            List<Subscription> list = this.subscribers.computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>());
            if (list.contains(subscription)) return false;
            return list.add(subscription);
        }

        void remove(EventPriority priority, Subscription subscription) {
            List<Subscription> list = this.subscribers.get(priority);
            if (list != null) list.remove(subscription);
        }

        boolean isEmpty() {
            return this.subscribers.values().stream().allMatch(List::isEmpty);
        }

        void registerToBukkit(EventPriority priority) {
            if (this.registeredListeners.containsKey(priority)) return;
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(
                    this.eventClass, listener, priority,
                    (l, event) -> {
                        if (!this.eventClass.isInstance(event)) return;
                        List<Subscription> subs = this.subscribers.get(priority);
                        if (subs == null) return;
                        T casted = this.eventClass.cast(event);
                        Map<String, Object> injected = Map.of("event", casted);
                        for (Subscription sub : subs) {
                            if (sub.ignoreCancelled() && casted instanceof Cancellable c && c.isCancelled()) continue;
                            // 事件本体既作为首个参数传入，也注入 event 绑定
                            sub.script().invoke(sub.function(), injected, casted);
                        }
                    },
                    BukkitScriptEventManager.this.plugin.javaPlugin(), false);
            this.registeredListeners.put(priority, listener);
        }

        void unregisterFromBukkit() {
            if (this.registeredListeners.isEmpty()) return;
            try {
                HandlerList handlerList = (HandlerList) this.eventClass.getMethod("getHandlerList").invoke(null);
                this.registeredListeners.values().forEach(handlerList::unregister);
            } catch (ReflectiveOperationException e) {
                BukkitScriptEventManager.this.plugin.logger().warn("Failed to unregister script event listeners of " + this.eventClass.getName(), e);
            }
            this.registeredListeners.clear();
        }
    }
}
