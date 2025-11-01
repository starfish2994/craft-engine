package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class L10NTag implements TagResolver {
    private final Context context;

    public L10NTag(Context context) {
        this.context = context;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.kyori.adventure.text.minimessage.Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        Locale locale = null;
        if (this.context instanceof PlayerOptionalContext playerOptionalContext && playerOptionalContext.isPlayerPresent()) {
            locale = playerOptionalContext.player().selectedLocale();
        }
        String i18nKey = arguments.popOr("No argument l10n key provided").toString();
        String translation = TranslationManager.instance().miniMessageTranslation(i18nKey, locale);
        return Tag.selfClosingInserting(AdventureHelper.miniMessage().deserialize(translation, this.context.tagResolvers()));
    }

    @Override
    public boolean has(@NotNull String name) {
        return "l10n".equals(name);
    }
}
