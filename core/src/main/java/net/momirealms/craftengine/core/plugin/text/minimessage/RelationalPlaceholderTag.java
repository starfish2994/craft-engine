package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.ViewerContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RelationalPlaceholderTag extends StaticTagResolver {
    public static final RelationalPlaceholderTag INSTANCE = new RelationalPlaceholderTag();

    private RelationalPlaceholderTag() {
        super("rel_papi");
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
            return null;
        }
        if (!(ctx.target() instanceof ViewerContext viewerContext)) {
            return null;
        }
        Player player1 = viewerContext.owner() instanceof PlayerOptionalContext ownerContext ? ownerContext.player() : null;
        Player player2 = viewerContext.viewer().player();
        if (player1 == null || player2 == null) {
            return null;
        }
        String rawArgument = arguments.popOr("No argument placeholder provided").toString();
        if (rawArgument.contains("<")) {
            rawArgument = AdventureHelper.plainTextContent(ctx.deserialize(rawArgument));
        }
        String placeholder = "%" + rawArgument + "%";
        String parsed = CraftEngine.instance().compatibilityManager().parse(player1, player2, placeholder);
        if (parsed.equals(placeholder)) {
            parsed = arguments.popOr("No default papi value provided").toString();
        }
        return Tag.selfClosingInserting(ctx.deserialize(parsed));
    }
}
