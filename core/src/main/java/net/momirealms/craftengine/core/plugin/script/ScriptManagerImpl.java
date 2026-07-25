package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.dependency.Dependencies;
import net.momirealms.craftengine.core.plugin.dependency.Dependency;
import net.momirealms.craftengine.core.plugin.script.annotation.AnnotatedFunction;
import net.momirealms.craftengine.core.plugin.script.annotation.ScriptAnnotation;
import net.momirealms.craftengine.core.plugin.script.annotation.ScriptAnnotationHandler;
import net.momirealms.craftengine.core.plugin.script.annotation.SubscribeAnnotationHandler;
import net.momirealms.craftengine.core.plugin.script.binding.LogBinding;
import net.momirealms.craftengine.core.plugin.script.binding.SchedulerBinding;
import net.momirealms.craftengine.core.plugin.script.binding.ScriptBinding;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventSubscriber;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ScriptManagerImpl implements ScriptManager {
    private final Plugin plugin;
    private final Map<String, ScriptBinding> bindings = new LinkedHashMap<>();
    private final Map<String, ScriptAnnotationHandler> annotationHandlers = new LinkedHashMap<>();
    private final Map<Key, ScriptFile> scripts = new ConcurrentHashMap<>();
    private final Map<String, ScriptFile> scriptsByName = new ConcurrentHashMap<>();
    private final Set<String> missingScriptWarned = ConcurrentHashMap.newKeySet();
    private JsEngine engine;
    private ScriptEventSubscriber eventSubscriber;
    private boolean available;

    public ScriptManagerImpl(Plugin plugin) {
        this.plugin = plugin;
        registerBinding(new SchedulerBinding(plugin));
        registerBinding(new LogBinding(plugin));
        registerAnnotationHandler(new SubscribeAnnotationHandler(this));
        if (!Config.enableJsScripting()) {
            this.available = false;
            return;
        }
        String engineName = Config.jsEngine().toLowerCase(java.util.Locale.ENGLISH);
        try {
            if (engineName.equalsIgnoreCase("nashorn")) {
                this.plugin.dependencyManager().loadDependencies(NASHORN_DEPENDENCIES);
                this.engine = new NashornScriptEngine();
            } else if (engineName.equalsIgnoreCase("graaljs")) {
                this.plugin.dependencyManager().loadDependencies(GRAALJS_DEPENDENCIES);
                this.engine = new GraalScriptEngine();
            } else {
                this.available = false;
                this.plugin.logger().warn("JS engine " + engineName + " not found.");
                return;
            }
            this.available = true;
            this.plugin.logger().info("JS engine: " + engineName);
        } catch (Throwable t) {
            this.plugin.logger().warn("JS engine '" + engineName + "' is unavailable, js scripting is disabled", t);
            this.available = false;
        }
    }

    private static final List<Dependency> GRAALJS_DEPENDENCIES = List.of(
            Dependencies.GRAALJS_POLYGLOT,
            Dependencies.GRAALJS_JS_LANGUAGE,
            Dependencies.GRAALJS_TRUFFLE_RUNTIME,
            Dependencies.GRAALJS_TRUFFLE_COMPILER,
            Dependencies.GRAALJS_TRUFFLE_API,
            Dependencies.GRAALJS_REGEX,
            Dependencies.GRAALJS_COLLECTIONS,
            Dependencies.GRAALJS_NATIVEIMAGE,
            Dependencies.GRAALJS_ICU4J
    );

    private static final List<Dependency> NASHORN_DEPENDENCIES = List.of(
            Dependencies.NASHORN_CORE,
            Dependencies.ASM_UTIL
    );

    public Plugin plugin() {
        return this.plugin;
    }

    Map<String, ScriptBinding> bindings() {
        return this.bindings;
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }

    @Override
    public void registerBinding(ScriptBinding binding) {
        this.bindings.put(binding.name(), binding);
    }

    public void registerAnnotationHandler(ScriptAnnotationHandler handler) {
        this.annotationHandlers.put(handler.name(), handler);
    }

    @Override
    public Optional<ScriptFile> script(String id) {
        if (id.indexOf(':') != -1) {
            Key key = Key.of(id);
            if (this.scripts.containsKey(key)) {
                return Optional.of(this.scripts.get(key));
            }
        }
        return Optional.ofNullable(this.scriptsByName.get(id));
    }

    @Override
    public Optional<ScriptFile> script(Key id) {
        return Optional.ofNullable(this.scripts.get(id));
    }

    @Override
    public void reload() {
        if (!this.available) return;
        unloadScripts();
        for (Pack pack : this.plugin.packManager().loadedPacks()) {
            if (!pack.enabled()) continue;
            for (Path scriptDir : pack.scriptFolders()) {
                scanScripts(scriptDir, pack.namespace());
            }
        }
        this.scripts.values().forEach(ScriptFile::warmUp);
        if (!this.scripts.isEmpty()) {
            this.plugin.logger().info("Loaded " + this.scripts.size() + " js script(s)");
        }
    }

    public void setEventSubscriber(ScriptEventSubscriber eventSubscriber) {
        this.eventSubscriber = eventSubscriber;
    }

    public void subscribeEvent(ScriptFile script, String eventClass, String function, @Nullable Map<String, Object> options) {
        if (this.eventSubscriber == null) {
            this.plugin.logger().warn("Script '" + script.id() + "' tried to subscribe event '" + eventClass + "', but event subscription is not supported on this platform");
            return;
        }
        this.eventSubscriber.subscribe(script, eventClass, function, options);
    }

    private void scanScripts(Path directory, String namespace) {
        if (!Files.isDirectory(directory)) return;
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(it -> it.getFileName().toString().endsWith(".js"))
                    .sorted()
                    .forEach(path -> {
                        String replaced = directory.relativize(path).toString().replace('\\', '/');
                        loadScript(path, Key.of(namespace, replaced.substring(replaced.length() - 3)));
                    });
        } catch (IOException e) {
            this.plugin.logger().warn("Failed to scan scripts directory " + directory, e);
        }
    }

    private void loadScript(Path path, Key id) {
        if (this.scripts.containsKey(id)) {
            this.plugin.logger().warn("Duplicated script id '" + id + "' (" + path + "), skipped");
            return;
        }
        try {
            ScriptFile script = new ScriptFile(this, this.engine, id, Files.readAllBytes(path));
            this.scripts.put(id, script);
            this.scriptsByName.put(id.value(), script);
            for (AnnotatedFunction annotated : script.annotatedFunctions()) {
                for (ScriptAnnotation annotation : annotated.annotations()) {
                    ScriptAnnotationHandler handler = this.annotationHandlers.get(annotation.name());
                    if (handler != null) {
                        try {
                            handler.handle(script, annotated.function(), annotation);
                        } catch (Throwable t) {
                            this.plugin.logger().warn("Error handling annotation '@" + annotation.name() + "' on function '" + annotated.function() + "' of script '" + id + "'", t);
                        }
                    } else {
                        this.plugin.logger().warn("Script '" + id + "' uses unknown annotation '@" + annotation.name() + "' on function '" + annotated.function() + "'");
                    }
                }
            }
        } catch (Throwable t) {
            this.plugin.logger().warn("Failed to load script '" + id + "'", t);
        }
    }

    @Override
    public void unload() {
        unloadScripts();
        if (this.engine != null) {
            this.engine.close();
            this.engine = null;
        }
    }

    private void unloadScripts() {
        this.scripts.values().forEach(ScriptFile::unload);
        this.scripts.clear();
        this.scriptsByName.clear();
        this.missingScriptWarned.clear();
    }

    @Override
    public @Nullable Object invoke(String id, String function, Context context, Map<String, Object> extras) {
        if (!this.available) return null;
        Optional<ScriptFile> script = this.script(id);
        if (script.isEmpty()) {
            // 缺失脚本只告警一次，避免热路径刷屏
            if (this.missingScriptWarned.add(id)) {
                this.plugin.logger().warn("Script '" + id + "' not found");
            }
            return null;
        }
        Map<String, Object> injected = new HashMap<>();
        context.contexts().params().forEach((key, supplier) -> {
            Object value = supplier.get();
            if (value != null) injected.put(key.node(), value);
        });
        injected.put("ctx", context);
        injected.putAll(extras);
        return script.get().invoke(function, injected);
    }

    @Override
    public boolean test(String id, String function, Context context, Map<String, Object> extras, boolean def) {
        Object result = invoke(id, function, context, extras);
        if (result instanceof Boolean bool) {
            return bool;
        }
        return result != null || def;
    }
}
