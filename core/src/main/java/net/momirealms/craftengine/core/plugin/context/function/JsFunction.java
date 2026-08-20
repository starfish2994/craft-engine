package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.script.ScriptManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String script;
    private final String function;
    private final Map<String, Object> args;

    private JsFunction(List<Condition<CTX>> predicates, String script, String function, Map<String, Object> args) {
        super(predicates);
        this.script = script.endsWith(".js") ? script.substring(0, script.length() - 3) : script;
        this.function = function;
        this.args = args;
    }

    @Override
    protected void runInternal(CTX ctx) {
        ScriptManager scriptManager = CraftEngine.instance().scriptManager();
        if (scriptManager == null || !scriptManager.isAvailable()) return;
        Map<String, Object> injected = ScriptManager.flattenContext(ctx);
        injected.putAll(this.args);
        scriptManager.invoke(this.script, this.function, injected);
    }

    public static <CTX extends Context> FunctionFactory<CTX, JsFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    public static Map<String, Object> parseArgs(ConfigSection section) {
        Object raw = section.get("args");
        if (raw instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>();
            ConfigSection argsSection = section.getSection("args");
            for (String key : argsSection.keySet()) {
                Object value = argsSection.get(key);
                if (value != null) map.put(key, value);
            }
            return map;
        }
        List<String> list = section.getStringList("args");
        return list.isEmpty() ? Map.of() : Map.of("args", list);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, JsFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public JsFunction<CTX> create(ConfigSection section) {
            return new JsFunction<>(
                    getPredicates(section),
                    section.getNonEmptyString("script"),
                    section.getNonEmptyString("function"),
                    parseArgs(section)
            );
        }
    }
}
