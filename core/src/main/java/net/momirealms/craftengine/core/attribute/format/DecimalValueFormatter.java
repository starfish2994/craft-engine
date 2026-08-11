package net.momirealms.craftengine.core.attribute.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record DecimalValueFormatter(DecimalFormat decimalFormat) implements ValueFormatter {
    public static final ValueFormatterFactory<DecimalValueFormatter> FACTORY = args -> ofPattern(args.getString("pattern", "#.##"));

    public static DecimalValueFormatter ofPattern(String pattern) {
        return new DecimalValueFormatter(new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US)));
    }

    @Override
    public String format(double value) {
        synchronized (this.decimalFormat) {
            return this.decimalFormat.format(value);
        }
    }
}
