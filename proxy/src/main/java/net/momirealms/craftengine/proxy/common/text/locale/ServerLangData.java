package net.momirealms.craftengine.proxy.common.text.locale;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ServerLangData {
    private final Map<Locale, String> translations = new HashMap<>();
    public final String fallback;

    public ServerLangData(String fallback) {
        this.fallback = fallback;
    }

    public ServerLangData() {
        this.fallback = null;
    }

    public void addTranslation(final Locale locale, final String translation) {
        this.translations.putIfAbsent(locale, translation);
    }

    public void addTranslations(Map<Locale, String> translations) {
        this.translations.putAll(translations);
    }

    public String translate(final Locale locale) {
        String translation = this.translations.get(locale);
        if (translation == null) {
            translation = this.translations.get(Locale.of(locale.getLanguage()));
            if (translation == null) {
                translation = this.fallback;
            }
        }
        return translation;
    }

    public int localeCount() {
        return this.translations.size();
    }

    public Map<Locale, String> getTranslations() {
        return Collections.unmodifiableMap(this.translations);
    }
}
