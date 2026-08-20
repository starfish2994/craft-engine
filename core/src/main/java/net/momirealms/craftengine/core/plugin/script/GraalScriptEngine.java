package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.HashUtils;
import net.momirealms.craftengine.core.util.Key;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GraalScriptEngine implements JsEngine {
    private final Engine engine;
    private final Context validationContext;
    private final Map<Key, CachedSource> sourceCache = new ConcurrentHashMap<>();

    public GraalScriptEngine() {
        this.engine = Engine.newBuilder()
                // 服务器 JVM 通常没开 JVMCI，静默 fallback 警告（解释执行仍可用）
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        this.validationContext = newContextBuilder().build();
    }

    @Override
    public JsCompiledScript compile(Key id, byte[] code) throws IOException {
        String sha1 = HashUtils.sha1(code);
        CachedSource cached = this.sourceCache.get(id);
        Source source;
        if (cached != null && cached.sha1().equals(sha1)) {
            source = cached.source();
        } else {
            source = Source.newBuilder("js", new String(code, StandardCharsets.UTF_8), id.asString()).build();
            // 加载期语法校验：只编译不执行，语法错误在此抛出 PolyglotException
            synchronized (this.validationContext) {
                this.validationContext.parse(source);
            }
            this.sourceCache.put(id, new CachedSource(sha1, source));
        }
        return new GraalCompiledScript(newContextBuilder().build(), source);
    }

    private Context.Builder newContextBuilder() {
        return Context.newBuilder("js")
                .engine(this.engine)
                .hostClassLoader(GraalScriptEngine.class.getClassLoader())
                .option("js.ecmascript-version", "latest")
                .option("js.strict", String.valueOf(Config.jsStrictMode()))
                .option("js.nashorn-compat", String.valueOf(Config.jsNashornCompat()))
                .allowAllAccess(true);
    }

    @Override
    public void close() {
        this.sourceCache.clear();
        this.validationContext.close();
        this.engine.close();
    }

    private record CachedSource(String sha1, Source source) {
    }
}
