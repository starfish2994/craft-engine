package net.momirealms.craftengine.core.plugin.script;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class NashornCompiledScript implements JsCompiledScript {
    private final ScriptEngine engine;
    private final CompiledScript compiled;
    private final Map<String, Object> globals = new LinkedHashMap<>();
    private boolean evaluated;
    private List<String> lastInjectedKeys = List.of();

    NashornCompiledScript(ScriptEngine engine, CompiledScript compiled) {
        this.engine = engine;
        this.compiled = compiled;
    }

    @Override
    public void putGlobal(String name, Object value) {
        this.globals.put(name, value);
        if (this.evaluated) {
            this.engine.put(name, value);
        }
    }

    @Override
    public void eval() throws Exception {
        if (this.evaluated) return;
        this.globals.forEach(this.engine::put);
        this.compiled.eval();
        this.evaluated = true;
    }

    @Override
    public boolean hasFunction(String function) {
        Object fn = this.engine.getBindings(ScriptContext.ENGINE_SCOPE).get(function);
        return fn instanceof ScriptObjectMirror mirror && mirror.isFunction();
    }

    @Override
    public @Nullable Object invoke(String function, Map<String, Object> injected, Object... args) throws Exception {
        eval();
        Bindings bindings = this.engine.getBindings(ScriptContext.ENGINE_SCOPE);
        // 注入的键在调用后不清除（延时任务的闭包可能引用），下次调用前清上一轮的
        this.lastInjectedKeys.forEach(bindings::remove);
        bindings.putAll(injected);
        this.lastInjectedKeys = List.copyOf(injected.keySet());
        return ((Invocable) this.engine).invokeFunction(function, args);
    }

    @Override
    public void close() {
        // Nashorn 无显式关闭，丢弃引用等待 GC
    }
}
