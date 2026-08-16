package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.damage.DamageEvent;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CompositionDamageFormula implements DamageFormula {
    public static final DamageFormulaFactory<CompositionDamageFormula> FACTORY = CompositionDamageFormula::new;
    private final Map<String, DamageFormula> parts;

    public CompositionDamageFormula(ConfigSection args) {
        ConfigSection partsSection = args.getNonNullSection("parts");
        Map<String, DamageFormula> parts = new LinkedHashMap<>();
        for (String key : partsSection.keySet()) {
            parts.put(key, partsSection.getValue(key, DamageFormulas::fromConfig));
        }
        this.parts = parts;
    }

    @Override
    public double getValue(DamageEvent event) {
        double total = 0;
        for (Map.Entry<String, DamageFormula> entry : this.parts.entrySet()) {
            double value = entry.getValue().getValue(event);
            event.recordDamagePart(entry.getKey(), value);
            total += value;
        }
        return total;
    }
}
