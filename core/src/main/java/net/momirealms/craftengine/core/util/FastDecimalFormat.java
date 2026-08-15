package net.momirealms.craftengine.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 线程安全的 DecimalFormat 子集实现，仅支持 '0' '#' '.' ','，HALF_UP 舍入，US 符号
 */
public final class FastDecimalFormat {
    private static final double FAST_LIMIT = 4.5e15; // 2^52，保证 floor/减法精确
    private static final long[] LONG_POW10 = new long[16];

    static {
        long v = 1;
        for (int i = 0; i < LONG_POW10.length; i++) {
            LONG_POW10[i] = v;
            v *= 10;
        }
    }

    private final int minIntDigits;
    private final int minFracDigits;
    private final int maxFracDigits;
    private final boolean grouping;
    private final boolean decimalAlwaysShown;
    private final double multiplier;

    public FastDecimalFormat(String pattern) {
        int dot = pattern.indexOf('.');
        if (dot != pattern.lastIndexOf('.')) {
            throw new IllegalArgumentException("Multiple decimal separators in pattern \"" + pattern + "\"");
        }
        String intPart = dot < 0 ? pattern : pattern.substring(0, dot);
        String fracPart = dot < 0 ? "" : pattern.substring(dot + 1);
        int minInt = 0;
        boolean grouping = false;
        boolean hasDigit = false;
        boolean hasIntHash = false;
        boolean hasIntZero = false;
        for (int i = 0; i < intPart.length(); i++) {
            char c = intPart.charAt(i);
            switch (c) {
                case '0' -> {
                    minInt++;
                    hasDigit = true;
                    hasIntZero = true;
                }
                case '#' -> {
                    hasDigit = true;
                    hasIntHash = true;
                }
                case ',' -> grouping = true;
                default -> throw new IllegalArgumentException("Malformed pattern \"" + pattern + "\"");
            }
        }
        int minFrac = 0;
        boolean hasFracZero = false;
        for (int i = 0; i < fracPart.length(); i++) {
            char c = fracPart.charAt(i);
            if (c == '0') {
                minFrac = i + 1;
                hasDigit = true;
                hasFracZero = true;
            } else if (c == '#') {
                hasDigit = true;
            } else {
                throw new IllegalArgumentException("Malformed pattern \"" + pattern + "\"");
            }
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Malformed pattern \"" + pattern + "\"");
        }
        // 与 DecimalFormat.applyPattern 对齐：含小数点且两侧都无 '0' 时，强制一侧至少显示一位
        if (dot >= 0 && !hasIntZero && !hasFracZero) {
            if (hasIntHash) minInt = 1;
            else minFrac = 1;
        }
        this.minIntDigits = minInt;
        this.minFracDigits = minFrac;
        this.maxFracDigits = fracPart.length();
        this.grouping = grouping;
        this.decimalAlwaysShown = dot == pattern.length() - 1;
        this.multiplier = Math.pow(10, this.maxFracDigits);
    }

    public String format(double value) {
        if (Double.isNaN(value)) return "NaN";
        boolean negative = Double.doubleToRawLongBits(value) < 0;
        double abs = Math.abs(value);
        if (Double.isInfinite(abs)) return negative ? "-∞" : "∞";
        if (abs < FAST_LIMIT && this.maxFracDigits <= 15) {
            // 拆成整数+小数分别处理：abs < 2^52 时 floor 与减法都精确，避免整体乘 10^f 丢低位
            long ip = (long) Math.floor(abs);
            long scaledFrac = Math.round((abs - ip) * this.multiplier);
            if (scaledFrac == LONG_POW10[this.maxFracDigits]) {
                ip++;
                scaledFrac = 0;
            }
            return formatFast(negative, ip, scaledFrac);
        }
        return formatBig(negative, abs);
    }

    private String formatFast(boolean negative, long ip, long frac) {
        char[] buf = new char[40];
        int pos = buf.length;
        int fracDigits = 0;
        if (this.maxFracDigits > 0) {
            fracDigits = this.maxFracDigits;
            while (fracDigits > this.minFracDigits && frac % 10 == 0) {
                frac /= 10;
                fracDigits--;
            }
            for (int i = 0; i < fracDigits; i++) {
                buf[--pos] = (char) ('0' + (int) (frac % 10));
                frac /= 10;
            }
            if (fracDigits > 0) buf[--pos] = '.';
        } else if (this.decimalAlwaysShown) {
            buf[--pos] = '.';
        }
        // minInt 为 0 且有可显示的小数位时，整数部分的 0 不显示（".5" 而非 "0.5"）
        boolean suppressZeroInt = ip == 0 && this.minIntDigits == 0 && fracDigits > 0;
        if (!suppressZeroInt) {
            int emitted = 0;
            do {
                if (this.grouping && emitted % 3 == 0 && emitted > 0) buf[--pos] = ',';
                buf[--pos] = (char) ('0' + (int) (ip % 10));
                ip /= 10;
                emitted++;
            } while (ip != 0);
            while (emitted < this.minIntDigits) {
                if (this.grouping && emitted % 3 == 0 && emitted > 0) buf[--pos] = ',';
                buf[--pos] = '0';
                emitted++;
            }
        }
        if (negative) buf[--pos] = '-';
        return new String(buf, pos, buf.length - pos);
    }

    private String formatBig(boolean negative, double abs) {
        String s = BigDecimal.valueOf(abs).setScale(this.maxFracDigits, RoundingMode.HALF_UP).toPlainString();
        int dot = s.indexOf('.');
        String intDigits = dot < 0 ? s : s.substring(0, dot);
        String fracDigits = dot < 0 ? "" : s.substring(dot + 1);
        int fracLen = fracDigits.length();
        while (fracLen > this.minFracDigits && fracDigits.charAt(fracLen - 1) == '0') fracLen--;
        if (this.minIntDigits == 0 && fracLen > 0 && intDigits.equals("0")) intDigits = "";
        StringBuilder sb = new StringBuilder(intDigits.length() + fracLen + 8);
        if (negative) sb.append('-');
        int intLen = Math.max(intDigits.length(), this.minIntDigits);
        int zeros = intLen - intDigits.length();
        int firstGroup = this.grouping ? intLen % 3 : -1;
        for (int i = 0; i < intLen; i++) {
            if (this.grouping && i > 0 && (i - firstGroup) % 3 == 0) sb.append(',');
            sb.append(i < zeros ? '0' : intDigits.charAt(i - zeros));
        }
        if (fracLen > 0) {
            sb.append('.').append(fracDigits, 0, fracLen);
        } else if (this.decimalAlwaysShown) {
            sb.append('.');
        }
        return sb.toString();
    }
}
