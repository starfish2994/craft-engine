package net.momirealms.craftengine.core.plugin.script;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GraalCompiledScript implements JsCompiledScript {
    private final Context context;
    private final Source source;
    private final Map<String, Object> globals = new LinkedHashMap<>();
    private boolean evaluated;
    private List<String> lastInjectedKeys = List.of();

    GraalCompiledScript(Context context, Source source) {
        this.context = context;
        this.source = source;
    }

    @Override
    public void putGlobal(String name, Object value) {
        this.globals.put(name, value);
        if (this.evaluated) {
            this.context.getBindings("js").putMember(name, value);
        }
    }

    @Override
    public void eval() {
        if (this.evaluated) return;
        Value bindings = this.context.getBindings("js");
        this.globals.forEach(bindings::putMember);
        this.context.eval(this.source);
        this.evaluated = true;
    }

    @Override
    public boolean hasFunction(String function) {
        Value fn = this.context.getBindings("js").getMember(function);
        return fn != null && fn.canExecute();
    }

    @Override
    public @Nullable Object invoke(String function, Map<String, Object> injected, Object... args) {
        eval();
        Value bindings = this.context.getBindings("js");
        this.lastInjectedKeys.forEach(bindings::removeMember);
        injected.forEach(bindings::putMember);
        this.lastInjectedKeys = List.copyOf(injected.keySet());
        return unwrap(bindings.getMember(function).execute(args));
    }

    @Override
    public void close() {
        try {
            this.context.close(true);
        } catch (Throwable ignored) {
        }
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
}
