package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.script.annotation.AnnotatedFunction;
import net.momirealms.craftengine.core.plugin.script.annotation.ScriptAnnotationParser;
import net.momirealms.craftengine.core.plugin.script.binding.SchedulerBinding;
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
    private final List<Runnable> unloadCallbacks = new CopyOnWriteArrayList<>();
    private final List<AnnotatedFunction> annotatedFunctions;
    private final Set<String> missingFunctionWarned = ConcurrentHashMap.newKeySet();
    private final Set<String> errorWarned = ConcurrentHashMap.newKeySet();
    private final List<DeclaredTask> declaredTasks = new CopyOnWriteArrayList<>();
    private final Set<ScriptTaskHandle> transientTasks = ConcurrentHashMap.newKeySet();

    ScriptFile(ScriptManagerImpl manager, JsEngine engine, Key id, byte[] code) throws Exception {
        this.manager = manager;
        this.id = id;
        this.compiled = engine.compile(id, code);
        this.compiled.putGlobal("__script", this);
        this.compiled.putGlobal("scheduler", new SchedulerBinding(manager.plugin(), this));
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

    public void registerTask(String function, long delayTicks, long periodTicks, boolean async) {
        this.declaredTasks.add(new DeclaredTask(function, delayTicks, periodTicks, async));
    }

    void startAutoTasks() {
        for (DeclaredTask task : this.declaredTasks) {
            ScriptTaskHandle handle = new ScriptTaskHandle(this);
            Runnable runnable = () -> invoke(task.function(), Map.of());
            handle.init(task.async()
                    ? this.manager.plugin().scheduler().platform().runAsyncRepeating(runnable, task.delayTicks(), task.periodTicks())
                    : this.manager.plugin().scheduler().platform().runRepeating(runnable, task.delayTicks(), task.periodTicks()));
        }
    }

    public void trackTask(ScriptTaskHandle handle) {
        this.transientTasks.add(handle);
    }

    public void untrackTask(ScriptTaskHandle handle) {
        this.transientTasks.remove(handle);
    }

    @Nullable
    public Object invoke(String function, Map<String, Object> injected, Object... args) {
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
            // 执行异常按函数去重告警
            if (this.errorWarned.add(function)) {
                this.manager.plugin().logger().warn("Error executing script '" + this.id + "::" + function + "' (further errors from this function will be suppressed)", e);
            }
            return null;
        }
    }

    void warmUp() {
        try {
            this.compiled.eval();
        } catch (Exception e) {
            this.manager.plugin().logger().warn("Error evaluating script '" + this.id + "'", e);
        }
    }

    void unload() {
        this.transientTasks.forEach(ScriptTaskHandle::cancel);
        this.transientTasks.clear();
        this.declaredTasks.clear();
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

    private record DeclaredTask(String function, long delayTicks, long periodTicks, boolean async) {
    }
}
