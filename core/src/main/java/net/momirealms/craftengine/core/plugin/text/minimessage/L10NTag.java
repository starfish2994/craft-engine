package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.pointer.Pointered;
import net.momirealms.craftengine.core.item.network.NetworkItemBuildContext;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class L10NTag extends StaticTagResolver {
    public static final L10NTag INSTANCE = new L10NTag();

    private L10NTag() {
        super("l10n");
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        Pointered target = ctx.target();
        if (target instanceof NetworkTextReplaceContext || target instanceof NetworkItemBuildContext) {
            return NetworkL10NTag.INSTANCE.resolve(name, arguments, ctx);
        }
        return PlainL10NTag.INSTANCE.resolve(name, arguments, ctx);
    }
}
