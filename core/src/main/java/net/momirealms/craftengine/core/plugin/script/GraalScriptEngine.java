package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.plugin.config.Config;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GraalScriptEngine {
    private final Engine engine;
    private final Map<String, CachedSource> sourceCache = new ConcurrentHashMap<>();

    GraalScriptEngine() {
        this.engine = Engine.newBuilder()
                // 服务器 JVM 通常没开 JVMCI，静默 fallback 警告（解释执行仍可用）
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    Context newContext() {
        Context.Builder builder = Context.newBuilder("js")
                .engine(this.engine)
                .hostClassLoader(GraalScriptEngine.class.getClassLoader())
                .option("js.ecmascript-version", Config.jsEcmascriptVersion())
                .option("js.strict", String.valueOf(Config.jsStrictMode()))
                .option("js.nashorn-compat", String.valueOf(Config.jsNashornCompat()));
        if (Config.jsAllowAllAccess()) {
            builder.allowAllAccess(true);
        } else {
            builder.allowHostAccess(org.graalvm.polyglot.HostAccess.EXPLICIT);
        }
        return builder.build();
    }

    Source source(String id, byte[] bytes) throws IOException {
        String sha1 = sha1(bytes);
        CachedSource cached = this.sourceCache.get(id);
        if (cached != null && cached.sha1().equals(sha1)) {
            return cached.source();
        }
        Source source = Source.newBuilder("js", new String(bytes, StandardCharsets.UTF_8), id).build();
        this.sourceCache.put(id, new CachedSource(sha1, source));
        return source;
    }

    void close() {
        this.sourceCache.clear();
        this.engine.close();
    }

    private static String sha1(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private record CachedSource(String sha1, Source source) {}
}
