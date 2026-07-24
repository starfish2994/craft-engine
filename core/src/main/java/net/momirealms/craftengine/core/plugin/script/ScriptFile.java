package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.script.binding.EventsBinding;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventHandler;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ScriptFile {
    private final ScriptManagerImpl manager;
    private final GraalScriptEngine engine;
    private final String id;
    private final Source source;
    private final Object lock = new Object();
    private final List<Runnable> unloadCallbacks = new CopyOnWriteArrayList<>();
    private List<String> lastInjectedKeys = List.of();
    private Context context;

    ScriptFile(ScriptManagerImpl manager, GraalScriptEngine engine, String id, byte[] code) throws IOException {
        this.manager = manager;
        this.engine = engine;
        this.id = id;
        this.source = engine.source(id, code);
    }

    @Nullable
    private static Object unwrap(Value value) {
        if (value == null || value.isNull()) return null;
        if (value.isBoolean()) return value.asBoolean();
        if (value.isString()) return value.asString();
        if (value.fitsInLong()) return value.asLong();
        if (value.fitsInDouble()) return value.asDouble();
        return value.as(Object.class);
    }

    public String id() {
        return this.id;
    }

    public void onUnload(Runnable callback) {
        this.unloadCallbacks.add(callback);
    }

    @Nullable
    Object invoke(String function, Map<String, Object> injected) {
        synchronized (this.lock) {
            Value bindings = graalContext().getBindings("js");
            // 注入的键在调用后不清除（延时任务的闭包可能引用），下次调用前清上一轮的
            this.lastInjectedKeys.forEach(bindings::removeMember);
            injected.forEach(bindings::putMember);
            this.lastInjectedKeys = List.copyOf(injected.keySet());
            try {
                Value fn = bindings.getMember(function);
                if (fn == null || !fn.canExecute()) {
                    this.manager.plugin().logger().warn("Script '" + this.id + "' has no executable function '" + function + "'");
                    return null;
                }
                return unwrap(fn.execute());
            } catch (PolyglotException e) {
                this.manager.plugin().logger().warn("Error executing script '" + this.id + "::" + function + "'", e);
                return null;
            }
        }
    }

    public void invokeHandler(ScriptEventHandler handler, Object event) {
        synchronized (this.lock) {
            graalContext();
            try {
                handler.handle(event);
            } catch (PolyglotException e) {
                this.manager.plugin().logger().warn("Error executing event handler of script '" + this.id + "'", e);
            }
        }
    }

    private Context graalContext() {
        if (this.context == null) {
            this.context = this.engine.newContext();
            Value bindings = this.context.getBindings("js");
            this.manager.bindings().forEach((name, binding) -> bindings.putMember(name, binding.value()));
            bindings.putMember("__script", this);
            bindings.putMember("events", new EventsBinding(this.manager, this));
            this.context.eval(this.source);
        }
        return this.context;
    }

    /**
     * 强制执行一次顶层代码（触发事件订阅等顶层副作用）
     */
    void warmUp() {
        synchronized (this.lock) {
            try {
                graalContext();
            } catch (PolyglotException e) {
                this.manager.plugin().logger().warn("Error evaluating script '" + this.id + "'", e);
            }
        }
    }

    void unload() {
        synchronized (this.lock) {
            for (Runnable callback : this.unloadCallbacks) {
                try {
                    callback.run();
                } catch (Throwable t) {
                    this.manager.plugin().logger().warn("Error while running unload callback of script '" + this.id + "'", t);
                }
            }
            this.unloadCallbacks.clear();
            if (this.context != null) {
                try {
                    this.context.close(true);
                } catch (Throwable ignored) {
                }
                this.context = null;
            }
        }
    }
}
