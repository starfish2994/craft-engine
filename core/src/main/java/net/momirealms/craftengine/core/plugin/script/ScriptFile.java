package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.script.annotation.AnnotatedFunction;
import net.momirealms.craftengine.core.plugin.script.annotation.ScriptAnnotationParser;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ScriptFile {
    private final ScriptManagerImpl manager;
    private final Key id;
    private final JsCompiledScript compiled;
    private final Object lock = new Object();
    private final List<Runnable> unloadCallbacks = new CopyOnWriteArrayList<>();
    private final List<AnnotatedFunction> annotatedFunctions;
    private final Set<String> missingFunctionWarned = ConcurrentHashMap.newKeySet();

    ScriptFile(ScriptManagerImpl manager, JsEngine engine, Key id, byte[] code) throws Exception {
        this.manager = manager;
        this.id = id;
        this.compiled = engine.compile(id, code);
        this.compiled.putGlobal("__script", this);
        manager.bindings().forEach((name, binding) -> this.compiled.putGlobal(name, binding.value()));
        this.annotatedFunctions = ScriptAnnotationParser.parse(new String(code, StandardCharsets.UTF_8));
    }

    public Key id() {
        return this.id;
    }

    public List<AnnotatedFunction> annotatedFunctions() {
        return this.annotatedFunctions;
    }

    public void onUnload(Runnable callback) {
        this.unloadCallbacks.add(callback);
    }

    @Nullable
    public Object invoke(String function, Map<String, Object> injected, Object... args) {
        synchronized (this.lock) {
            try {
                this.compiled.eval();
                if (!this.compiled.hasFunction(function)) {
                    // 缺失函数只告警一次，避免热路径刷屏
                    if (this.missingFunctionWarned.add(function)) {
                        this.manager.plugin().logger().warn("Script '" + this.id + "' has no executable function '" + function + "'");
                    }
                    return null;
                }
                return this.compiled.invoke(function, injected, args);
            } catch (Exception e) {
                this.manager.plugin().logger().warn("Error executing script '" + this.id + "::" + function + "'", e);
                return null;
            }
        }
    }

    void warmUp() {
        synchronized (this.lock) {
            try {
                this.compiled.eval();
            } catch (Exception e) {
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
            this.compiled.close();
        }
    }
}
