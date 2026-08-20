package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.Key;

public interface JsEngine {

    JsCompiledScript compile(Key id, byte[] code) throws Exception;

    void close();
}
