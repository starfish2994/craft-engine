package net.momirealms.craftengine.core.plugin.script;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface JsCompiledScript {

    void putGlobal(String name, Object value);

    void eval() throws Exception;

    boolean hasFunction(String function);

    @Nullable
    Object invoke(String function, Map<String, Object> injected, Object... args) throws Exception;

    void close();
}
