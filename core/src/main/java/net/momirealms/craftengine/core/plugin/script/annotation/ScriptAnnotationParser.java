package net.momirealms.craftengine.core.plugin.script.annotation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScriptAnnotationParser {

    private ScriptAnnotationParser() {
    }

    public static List<AnnotatedFunction> parse(String code) {
        List<AnnotatedFunction> result = new ArrayList<>();
        List<ScriptAnnotation> pending = new ArrayList<>();
        for (String line : code.split("\\R")) {
            String trimmed = line.trim();
            ScriptAnnotation annotation = parseAnnotationLine(trimmed);
            if (annotation != null) {
                pending.add(annotation);
                continue;
            }
            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                continue;
            }
            String functionName = parseFunctionName(trimmed);
            if (functionName != null && !pending.isEmpty()) {
                result.add(new AnnotatedFunction(functionName, List.copyOf(pending)));
            }
            pending.clear();
        }
        return result;
    }

    @Nullable
    private static ScriptAnnotation parseAnnotationLine(String line) {
        if (!line.startsWith("//@")) return null;
        int i = 3;
        int start = i;
        while (i < line.length() && (Character.isJavaIdentifierPart(line.charAt(i)))) i++;
        if (i == start) return null;
        String name = line.substring(start, i);
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
        if (i >= line.length()) {
            return new ScriptAnnotation(name, List.of(), Map.of(), "");
        }
        if (line.charAt(i) != '(') return null;
        int close = findClosingParen(line, i);
        if (close < 0) return null;
        String rawArgs = line.substring(i + 1, close);
        return parseArgs(name, rawArgs);
    }

    private static int findClosingParen(String line, int open) {
        int depth = 0;
        char quote = 0;
        boolean escape = false;
        for (int i = open; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (quote != 0) {
                if (c == '\\') escape = true;
                else if (c == quote) quote = 0;
                continue;
            }
            switch (c) {
                case '"', '\'', '`' -> quote = c;
                case '\\' -> escape = true;
                case '(' -> depth++;
                case ')' -> {
                    if (--depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static ScriptAnnotation parseArgs(String name, String rawArgs) {
        List<String> positional = new ArrayList<>();
        Map<String, Object> named = new HashMap<>();
        for (String part : splitTopLevel(rawArgs)) {
            if (part.isBlank()) continue;
            int separator = findTopLevelSeparator(part);
            if (separator > 0) {
                String key = part.substring(0, separator).trim();
                String value = part.substring(separator + 1).trim();
                named.put(key, parseValue(value));
            } else {
                positional.add(unquote(part.trim()));
            }
        }
        return new ScriptAnnotation(name, List.copyOf(positional), Map.copyOf(named), rawArgs);
    }

    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        char quote = 0;
        boolean escape = false;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (quote != 0) {
                if (c == '\\') escape = true;
                else if (c == quote) quote = 0;
                continue;
            }
            switch (c) {
                case '"', '\'', '`' -> quote = c;
                case '\\' -> escape = true;
                case '(', '[', '{' -> depth++;
                case ')', ']', '}' -> depth--;
                case ',' -> {
                    if (depth == 0) {
                        parts.add(s.substring(start, i));
                        start = i + 1;
                    }
                }
            }
        }
        parts.add(s.substring(start));
        return parts;
    }

    private static int findTopLevelSeparator(String s) {
        int depth = 0;
        char quote = 0;
        boolean escape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (quote != 0) {
                if (c == '\\') escape = true;
                else if (c == quote) quote = 0;
                continue;
            }
            switch (c) {
                case '"', '\'', '`' -> quote = c;
                case '\\' -> escape = true;
                case '(', '[', '{' -> depth++;
                case ')', ']', '}' -> depth--;
                case ':', '=' -> {
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static Object parseValue(String s) {
        if (s.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (s.equalsIgnoreCase("false")) return Boolean.FALSE;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
        }
        return unquote(s);
    }

    private static String unquote(String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'') || (first == '`' && last == '`')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    @Nullable
    private static String parseFunctionName(String line) {
        int idx = indexOfWord(line, "function");
        if (idx >= 0) {
            int i = idx + "function".length();
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            int start = i;
            while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) i++;
            if (i > start) return line.substring(start, i);
            return null;
        }
        for (String keyword : new String[]{"const", "let", "var"}) {
            int k = indexOfWord(line, keyword);
            if (k < 0) continue;
            int i = k + keyword.length();
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            int start = i;
            while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) i++;
            if (i == start) return null;
            String name = line.substring(start, i);
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            if (i >= line.length() || line.charAt(i) != '=') return null;
            i++;
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            if (i >= line.length()) return null;
            char c = line.charAt(i);
            if (c == '(') return name;
            if (line.startsWith("async", i)) return name;
            int j = i;
            while (j < line.length() && Character.isJavaIdentifierPart(line.charAt(j))) j++;
            while (j < line.length() && Character.isWhitespace(line.charAt(j))) j++;
            if (j > i && line.startsWith("=>", j)) return name;
            return null;
        }
        return null;
    }

    private static int indexOfWord(String line, String word) {
        if (!line.startsWith(word)) {
            if (!line.startsWith("export ")) return -1;
            int i = "export ".length();
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
            String rest = line.substring(i);
            return rest.startsWith(word) ? i : -1;
        }
        int end = word.length();
        if (line.length() > end && Character.isJavaIdentifierPart(line.charAt(end))) return -1;
        return 0;
    }
}
