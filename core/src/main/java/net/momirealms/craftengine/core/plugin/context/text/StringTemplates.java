package net.momirealms.craftengine.core.plugin.context.text;

import net.momirealms.craftengine.core.plugin.context.Context;

public final class StringTemplates {
    private StringTemplates() {
    }

    public static String render(String raw, Context context) {
        if (raw.indexOf('<') == -1 || raw.lastIndexOf('>') == -1) {
            return raw;
        }
        return StringTemplate.of(raw).render(context);
    }
}
