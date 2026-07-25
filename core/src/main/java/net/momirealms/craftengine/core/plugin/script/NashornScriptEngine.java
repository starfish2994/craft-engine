package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.Key;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class NashornScriptEngine implements JsEngine {
    private final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

    @Override
    public JsCompiledScript compile(Key id, byte[] code) throws Exception {
        List<String> args = new ArrayList<>(List.of("--language=es6", "-doe"));
        if (net.momirealms.craftengine.core.plugin.config.Config.jsStrictMode()) {
            args.add("-strict");
        }
        ScriptEngine engine = this.factory.getScriptEngine(args.toArray(new String[0]), NashornScriptEngine.class.getClassLoader());
        engine.put(ScriptEngine.FILENAME, id);
        CompiledScript compiled = ((Compilable) engine).compile(new String(code, StandardCharsets.UTF_8));
        return new NashornCompiledScript(engine, compiled);
    }

    @Override
    public void close() {
        // 引擎实例由各个 NashornCompiledScript 持有并释放，无共享状态
    }
}
