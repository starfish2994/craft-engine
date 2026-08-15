package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTag extends StaticTagResolver {
    public static final PlaceholderTag INSTANCE = new PlaceholderTag("papi");

    protected PlaceholderTag(String name) {
        super(name);
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
            return null;
        }
        if (!(ctx.target() instanceof net.momirealms.craftengine.core.plugin.context.Context context)) {
            return null;
        }
        String rawArgument = arguments.popOr("No argument relational placeholder provided").toString();
        if (rawArgument.contains("<")) {
            rawArgument = AdventureHelper.plainTextContent(ctx.deserialize(rawArgument));
        }
        String placeholder = "%" + rawArgument + "%";
        Player player = playerFor(context);
        String parsed = player != null
                ? CraftEngine.instance().compatibilityManager().parse(player, placeholder)
                : CraftEngine.instance().compatibilityManager().parse(null, placeholder);
        if (parsed.equals(placeholder)) {
            parsed = arguments.popOr("No default papi value provided").toString();
        }
        return Tag.selfClosingInserting(ctx.deserialize(parsed));
    }

    protected @Nullable Player playerFor(net.momirealms.craftengine.core.plugin.context.Context context) {
        return context instanceof PlayerContext playerContext ? playerContext.player() : null;
    }

}
