package net.momirealms.craftengine.core.attribute.format;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 固定小数位数格式化，线程安全（无状态）。
 * digits=2 & trim: 3.14159 → "3.14"，3.0 → "3"；trim=false 时 3.0 → "3.00"。
 * shift 为小数点右移位数、suffix 为后缀，二者组合出百分比等用法：shift=2 & suffix="%" 时 0.055 → "5.5%"。
 */
public record FixedValueFormatter(int digits, boolean trim, int shift, String suffix) implements ValueFormatter {
    public static final ValueFormatterFactory<FixedValueFormatter> FACTORY = args -> new FixedValueFormatter(
            args.getInt("digits", 2),
            args.getBoolean("trim", true),
            args.getInt("shift", 0),
            args.getString("suffix", "")
    );
    public static final ValueFormatterFactory<FixedValueFormatter> PERCENT_FACTORY = args -> new FixedValueFormatter(
            args.getInt("digits", 2),
            args.getBoolean("trim", true),
            2,
            args.getString("suffix", "%")
    );

    @Override
    public String format(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        if (this.shift != 0) {
            // movePointRight 是十进制精确移位，避免 value * 100 的浮点噪声
            decimal = decimal.movePointRight(this.shift);
        }
        decimal = decimal.setScale(this.digits, RoundingMode.HALF_UP);
        String result;
        if (!this.trim) {
            result = decimal.toPlainString();
        } else if (decimal.signum() == 0) {
            result = "0";
        } else {
            result = decimal.stripTrailingZeros().toPlainString();
        }
        return this.suffix.isEmpty() ? result : result + this.suffix;
    }
}
