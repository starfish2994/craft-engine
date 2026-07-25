package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.script.ScriptManager;

import java.util.List;
import java.util.Map;

public final class JsFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String script;
    private final String function;
    private final List<String> args;

    private JsFunction(List<Condition<CTX>> predicates, String script, String function, List<String> args) {
        super(predicates);
        this.script = script.endsWith(".js") ? script.substring(0, script.length() - 3) : script;
        this.function = function;
        this.args = args;
    }

    @Override
    protected void runInternal(CTX ctx) {
        ScriptManager scriptManager = CraftEngine.instance().scriptManager();
        if (scriptManager == null || !scriptManager.isAvailable()) return;
        scriptManager.invoke(this.script, this.function, ctx, this.args.isEmpty() ? Map.of() : Map.of("args", this.args));
    }

    public static <CTX extends Context> FunctionFactory<CTX, JsFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
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
                    section.getStringList("args")
            );
        }
    }
}
