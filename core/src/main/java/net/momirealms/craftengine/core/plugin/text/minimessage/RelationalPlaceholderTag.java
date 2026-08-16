package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.ViewerContext;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.plugin.context.text.StringTemplates;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code <rel_papi:placeholder[:default]>} tag — relational PlaceholderAPI placeholder
 * (owner × viewer). One class, two native entry points.
 */
public final class RelationalPlaceholderTag extends StaticTagResolver implements StringTag {
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

    @Override
    public @Nullable String resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
            return null;
        }
        if (!(context instanceof ViewerContext viewerContext)) {
            return null;
        }
        Player player1 = viewerContext.owner() instanceof PlayerOptionalContext ownerContext ? ownerContext.player() : null;
        Player player2 = viewerContext.viewer().player();
        if (player1 == null || player2 == null) {
            return null;
        }
        String raw = StringTag.requireArg(args, 0, "No argument relational placeholder provided");
        if (raw.contains("<")) {
            raw = StringTemplates.render(raw, context);
        }
        String placeholder = "%" + raw + "%";
        String parsed = CraftEngine.instance().compatibilityManager().parse(player1, player2, placeholder);
        if (parsed.equals(placeholder)) {
            parsed = StringTag.requireArg(args, 1, "No default papi value provided");
        }
        return parsed;
    }

    @Override
    public StringTag precompile(String[] args) {
        final String raw = StringTag.requireArg(args, 0, "No argument relational placeholder provided");
        final String staticPlaceholder = raw.contains("<") ? null : "%" + raw + "%";
        final net.momirealms.craftengine.core.plugin.context.text.StringTemplate dynamicTemplate =
                staticPlaceholder == null ? net.momirealms.craftengine.core.plugin.context.text.StringTemplate.of(raw) : null;
        final String fallback = args.length > 1 ? args[1] : null;
        return (boundArgs, context) -> {
            if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
                return null;
            }
            if (!(context instanceof ViewerContext viewerContext)) {
                return null;
            }
            Player player1 = viewerContext.owner() instanceof PlayerOptionalContext ownerContext ? ownerContext.player() : null;
            Player player2 = viewerContext.viewer().player();
            if (player1 == null || player2 == null) {
                return null;
            }
            String placeholder = staticPlaceholder != null
                    ? staticPlaceholder
                    : "%" + dynamicTemplate.render(context) + "%";
            String parsed = CraftEngine.instance().compatibilityManager().parse(player1, player2, placeholder);
            if (parsed.equals(placeholder)) {
                if (fallback == null) {
                    throw new IllegalArgumentException("No default papi value provided");
                }
                parsed = fallback;
            }
            return parsed;
        };
    }
}
