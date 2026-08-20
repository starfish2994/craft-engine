package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NetworkL10NTag extends StaticTagResolver {
    public static final NetworkL10NTag INSTANCE = new NetworkL10NTag();

    private NetworkL10NTag() {
        super("l10n");
    }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue aq, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        Locale locale = null;
        if (ctx.target() instanceof PlayerOptionalContext playerOptionalContext && playerOptionalContext.isPlayerPresent()) {
            locale = playerOptionalContext.player().selectedLocale();
        }
        String l10n = aq.popOr("No argument l10n key provided").toString();
        String translation = TranslationManager.instance().miniMessageTranslation(l10n, locale);
        if (aq.hasNext()) {
            List<Component> arguments = new ArrayList<>();
            while (aq.hasNext()) {
                Tag.Argument arg = aq.pop();
                arguments.add(ctx.deserialize(arg.value()));
            }
            return Tag.selfClosingInserting(ctx.deserialize(translation, new IndexedArgumentTag(arguments)));
        } else {
            return Tag.selfClosingInserting(ctx.deserialize(translation));
        }
    }

}
