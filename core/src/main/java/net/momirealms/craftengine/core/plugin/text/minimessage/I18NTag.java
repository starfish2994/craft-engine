package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import net.momirealms.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public final class I18NTag extends StaticTagResolver {
    public static final TagResolver INSTANCE = new I18NTag();

    private I18NTag() { super("i18n"); }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        String i18nKey = arguments.popOr("No argument i18n key provided").toString();
        String translation = TranslationManager.instance().miniMessageTranslation(i18nKey);
        return Tag.selfClosingInserting(ctx.deserialize(translation));
    }
}
