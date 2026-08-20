package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ConfigKeys {

    private ConfigKeys() {
    }

    /**
     * 传入 snake_case 规范键，返回 snake_case + kebab-case 双写法数组。
     * 支持 (xxx) 可选段（"state(s)" → state/states）与 | 分支（"entit(y|ies)" → entity/entities）
     */
    public static String[] of(String key) {
        return of(new String[]{key});
    }

    public static String[] of(String... keys) {
        List<String> result = new ArrayList<>(keys.length * 2);
        for (String key : keys) {
            expand(key, result);
        }
        return result.toArray(String[]::new);
    }

    private static void expand(String key, List<String> out) {
        int depth = 0;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == '|' && depth == 0) {
                expand(key.substring(0, i), out);
                expand(key.substring(i + 1), out);
                return;
            }
        }
        int open = key.indexOf('(');
        int close = open == -1 ? -1 : key.indexOf(')', open);
        if (close == -1) {
            addWithKebab(key, out);
            return;
        }
        String prefix = key.substring(0, open);
        String content = key.substring(open + 1, close);
        String suffix = key.substring(close + 1);
        if (content.indexOf('|') >= 0) {
            for (String alt : StringUtils.split(content, '|')) {
                expand(prefix + alt + suffix, out);
            }
        } else {
            expand(prefix + suffix, out);
            expand(prefix + content + suffix, out);
        }
    }

    private static void addWithKebab(String key, List<String> out) {
        out.add(key);
        String alt = key.replace('_', '-');
        if (alt != key) {
            out.add(alt);
        }
    }
}
