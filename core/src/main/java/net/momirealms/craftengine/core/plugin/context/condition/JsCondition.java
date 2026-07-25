package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.script.ScriptManager;

import java.util.List;
import java.util.Map;

public final class JsCondition<CTX extends Context> implements Condition<CTX> {
    private final String script;
    private final String function;
    private final List<String> args;

    private JsCondition(String script, String function, List<String> args) {
        this.script = script.endsWith(".js") ? script.substring(0, script.length() - 3) : script;
        this.function = function;
        this.args = args;
    }

    @Override
    public boolean test(CTX ctx) {
        ScriptManager scriptManager = CraftEngine.instance().scriptManager();
        if (scriptManager == null || !scriptManager.isAvailable()) return false;
        return scriptManager.test(this.script, this.function, ctx, this.args.isEmpty() ? Map.of() : Map.of("args", this.args), false);
    }

    public static <CTX extends Context> ConditionFactory<CTX, JsCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, JsCondition<CTX>> {

        @Override
        public JsCondition<CTX> create(ConfigSection section) {
            return new JsCondition<>(
                    section.getNonEmptyString("script"),
                    section.getString("function", "main"),
                    section.getStringList("args")
            );
        }
    }
}
