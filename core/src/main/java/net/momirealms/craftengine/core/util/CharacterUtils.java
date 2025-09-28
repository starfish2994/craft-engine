package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class CharacterUtils {
    private static final Pattern PATTERN = Pattern.compile("[\\p{Mn}\\p{Me}\\p{Mc}\\p{Cf}]");

    private CharacterUtils() {}

    public static char[] decodeUnicodeToChars(String unicodeString) {
        int count = unicodeString.length() / 6;
        if (unicodeString.length() % 6 != 0) {
            throw new LocalizedResourceConfigException("warning.config.image.invalid_unicode_string_length");
        }
        char[] chars = new char[count];
        for (int i = 0, j = 0; j < count; i += 6, j++) {
            String hex = unicodeString.substring(i + 2, i + 6);
            try {
                chars[j] = (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                throw new LocalizedResourceConfigException("warning.config.image.invalid_hex_value", e, hex);
            }
        }
        return chars;
    }

    public static int charsToCodePoint(char[] chars) {
        if (chars.length == 1) {
            return chars[0];
        } else if (chars.length == 2) {
            if (Character.isHighSurrogate(chars[0]) && Character.isLowSurrogate(chars[1])) {
                return Character.toCodePoint(chars[0], chars[1]);
            } else {
                throw new IllegalArgumentException("Invalid surrogate pair: not a valid high and low surrogate combination.");
            }
        } else {
            throw new IllegalArgumentException("The given chars array must contain either 1 or 2 characters.");
        }
    }

    public static int[] charsToCodePoints(char[] chars) {
        return IntStream.range(0, chars.length)
                .filter(i -> !Character.isLowSurrogate(chars[i]))
                .map(i -> {
                    char c1 = chars[i];
                    if (Character.isHighSurrogate(c1)) {
                        if (i + 1 < chars.length && Character.isLowSurrogate(chars[i + 1])) {
                            char c2 = chars[++i];
                            return Character.toCodePoint(c1, c2);
                        } else {
                            throw new IllegalArgumentException("Illegal surrogate pair: High surrogate without matching low surrogate at index " + i);
                        }
                    } else {
                        return c1;
                    }
                }).toArray();
    }

    public static String encodeCharToUnicode(char c) {
        return String.format("\\u%04x", (int) c);
    }

    public static String encodeCharsToUnicode(char[] chars) {
        StringBuilder builder = new StringBuilder();
        for (char value : chars) {
            builder.append(encodeCharToUnicode(value));
        }
        return builder.toString();
    }

    public static boolean containsCombinedCharacter(String input) {
        if (input == null || input.isEmpty() || input.length() == 1) return false;
        for (int i = 0; i < input.length();) {
            int codePoint = input.codePointAt(i);
            i += Character.charCount(codePoint);
            int type = Character.getType(codePoint);
            if (type == Character.NON_SPACING_MARK ||
                    type == Character.ENCLOSING_MARK ||
                    type == Character.COMBINING_SPACING_MARK ||
                    type == Character.FORMAT ||
                    type == Character.CONTROL ||
                    type == Character.SURROGATE ||
                    type == Character.PRIVATE_USE ||
                    PATTERN.matcher(new String(Character.toChars(codePoint))).find()
            ) return true;
            if (i < input.length()) {
                int nextCodePoint = input.codePointAt(i);
                if (Character.isSurrogatePair(Character.toChars(codePoint)[0], Character.toChars(nextCodePoint)[0])) return true;
            }
        }
        return false;
    }

    public static String replaceBackslashWithSlash(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(c == '\\' ? '/' : c);
        }
        return sb.toString();
    }

    public static String replaceDoubleBackslashU(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int length = input.length();
        StringBuilder sb = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            if (i + 2 < length
                    && input.charAt(i) == '\\'
                    && input.charAt(i + 1) == '\\'
                    && input.charAt(i + 2) == 'u') {
                sb.append("\\u");
                i += 3;
            } else {
                sb.append(input.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
