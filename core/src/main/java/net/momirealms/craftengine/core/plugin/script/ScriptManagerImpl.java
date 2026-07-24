package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.dependency.Dependencies;
import net.momirealms.craftengine.core.plugin.dependency.Dependency;
import net.momirealms.craftengine.core.plugin.script.binding.LogBinding;
import net.momirealms.craftengine.core.plugin.script.binding.SchedulerBinding;
import net.momirealms.craftengine.core.plugin.script.binding.ScriptBinding;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventHandler;
import net.momirealms.craftengine.core.plugin.script.event.ScriptEventSubscriber;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ScriptManagerImpl implements ScriptManager {
    private final Plugin plugin;
    private final Map<String, ScriptBinding> bindings = new LinkedHashMap<>();
    private final Map<String, ScriptFile> scripts = new ConcurrentHashMap<>();
    private GraalScriptEngine engine;
    private ScriptEventSubscriber eventSubscriber;
    private boolean available;

    public ScriptManagerImpl(Plugin plugin) {
        this.plugin = plugin;
        if (!Config.enableJsScripting()) {
            this.available = false;
            return;
        }
        try {
            this.plugin.dependencyManager().loadDependencies(GRAALJS_DEPENDENCIES);
            this.engine = new GraalScriptEngine();
            this.available = true;
        } catch (Throwable t) {
            this.plugin.logger().warn("GraalJS engine is unavailable, js scripting is disabled", t);
            this.available = false;
        }
        registerBinding(new SchedulerBinding(plugin));
        registerBinding(new LogBinding(plugin));
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

    Plugin plugin() {
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

    @Override
    public Optional<ScriptFile> script(String id) {
        return Optional.ofNullable(this.scripts.get(id));
    }

    @Override
    public void reload() {
        if (!this.available) return;
        unloadScripts();
        for (Pack pack : this.plugin.packManager().loadedPacks()) {
            if (!pack.enabled()) continue;
            for (Path scriptDir : pack.scriptFolders()) {
                scanScripts(scriptDir, pack.namespace() + "/");
            }
        }
        this.scripts.values().forEach(ScriptFile::warmUp);
    }

    public void setEventSubscriber(ScriptEventSubscriber eventSubscriber) {
        this.eventSubscriber = eventSubscriber;
    }

    public void subscribeEvent(ScriptFile script, Class<?> eventClass, ScriptEventHandler handler, @Nullable Map<String, Object> options) {
        if (this.eventSubscriber == null) {
            this.plugin.logger().warn("Script '" + script.id() + "' tried to subscribe event '" + eventClass.getName() + "', but event subscription is not supported on this platform");
            return;
        }
        this.eventSubscriber.subscribe(script, eventClass, handler, options);
    }

    private void scanScripts(Path directory, String idPrefix) {
        if (!Files.isDirectory(directory)) return;
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(it -> it.getFileName().toString().endsWith(".js"))
                    .forEach(path -> {
                        String id = idPrefix + directory.relativize(path).toString().replace('\\', '/');
                        loadScript(path, id);
                    });
        } catch (IOException e) {
            this.plugin.logger().warn("Failed to scan scripts directory " + directory, e);
        }
    }

    private void loadScript(Path path, String id) {
        if (this.scripts.containsKey(id)) {
            this.plugin.logger().warn("Duplicated script id '" + id + "' (" + path + "), skipped");
            return;
        }
        try {
            this.scripts.put(id, new ScriptFile(this, this.engine, id, Files.readAllBytes(path)));
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
    }

    @Override
    public @Nullable Object invoke(String id, String function, Context context, Map<String, Object> extras) {
        if (!this.available) return null;
        ScriptFile script = this.scripts.get(id);
        if (script == null) {
            this.plugin.logger().warn("Script '" + id + "' not found");
            return null;
        }
        Map<String, Object> injected = new HashMap<>();
        context.contexts().params().forEach((key, supplier) -> {
            Object value = supplier.get();
            if (value != null) injected.put(key.node(), value);
        });
        injected.put("ctx", context);
        injected.putAll(extras);
        return script.invoke(function, injected);
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
