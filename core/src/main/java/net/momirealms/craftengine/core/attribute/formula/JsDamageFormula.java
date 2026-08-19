package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.damage.DamageEvent;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.function.JsFunction;
import net.momirealms.craftengine.core.plugin.script.ScriptManager;

import java.util.Map;

public final class JsDamageFormula implements DamageFormula {
    public static final DamageFormulaFactory<JsDamageFormula> FACTORY = JsDamageFormula::new;

    private final String script;
    private final String function;
    private final Map<String, Object> args;

    private JsDamageFormula(ConfigSection section) {
        String script = section.getNonEmptyString("script");
        this.script = script.endsWith(".js") ? script.substring(0, script.length() - 3) : script;
        this.function = section.getString("function", "main");
        this.args = JsFunction.parseArgs(section);
    }

    @Override
    public double getValue(DamageEvent event) {
        ScriptManager scriptManager = CraftEngine.instance().scriptManager();
        if (scriptManager == null || !scriptManager.isAvailable()) {
            return event.damage();
        }

        Map<String, Object> injected = ScriptManager.flattenContext(event.context());
        injected.put("event", event);
        injected.putAll(this.args);

        Object result = scriptManager.invoke(this.script, this.function, injected);
        return result instanceof Number number ? number.doubleValue() : event.damage();
    }
}
