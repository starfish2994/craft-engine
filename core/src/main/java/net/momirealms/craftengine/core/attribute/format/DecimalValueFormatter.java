package net.momirealms.craftengine.core.attribute.format;

import net.momirealms.craftengine.core.util.FastDecimalFormat;

public record DecimalValueFormatter(FastDecimalFormat decimalFormat) implements ValueFormatter {
    public static final ValueFormatterFactory<DecimalValueFormatter> FACTORY = args -> ofPattern(args.getString("pattern", "#.##"));

    public static DecimalValueFormatter ofPattern(String pattern) {
        return new DecimalValueFormatter(new FastDecimalFormat(pattern));
    }

    @Override
    public String format(double value) {
        return this.decimalFormat.format(value);
    }
}
