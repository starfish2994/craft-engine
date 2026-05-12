package net.momirealms.craftengine.proxy.common.text.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.proxy.common.context.NetworkTextReplaceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NetworkL10NTag implements TagResolver {
    private final NetworkTextReplaceContext context;

    public NetworkL10NTag(NetworkTextReplaceContext context) {
        this.context = context;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue aq, @NotNull net.kyori.adventure.text.minimessage.Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        Locale locale = context.player().locale();
        String l10n = aq.popOr("No argument l10n key provided").toString();
        String translation = context.tagData().miniMessageTranslation(l10n, locale);
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

    @Override
    public boolean has(@NotNull String name) {
        return "l10n".equals(name);
    }
}
