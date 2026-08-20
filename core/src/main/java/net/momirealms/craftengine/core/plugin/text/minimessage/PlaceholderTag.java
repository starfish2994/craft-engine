package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.plugin.context.text.StringTemplates;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTag extends StaticTagResolver implements StringTag {
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
        String rawArgument = arguments.popOr("No argument placeholder provided").toString();
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

    @Override
    public @Nullable String resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
            return null;
        }
        String raw = StringTag.requireArg(args, 0, "No argument placeholder provided");
        if (raw.contains("<")) {
            raw = StringTemplates.render(raw, context);
        }
        String placeholder = "%" + raw + "%";
        Player player = playerFor(context);
        String parsed = player != null
                ? CraftEngine.instance().compatibilityManager().parse(player, placeholder)
                : CraftEngine.instance().compatibilityManager().parse(null, placeholder);
        if (parsed.equals(placeholder)) {
            parsed = StringTag.requireArg(args, 1, "No default papi value provided");
        }
        return parsed;
    }

    @Override
    public StringTag precompile(String[] args) {
        final String raw = StringTag.requireArg(args, 0, "No argument placeholder provided");
        final String staticPlaceholder = raw.contains("<") ? null : "%" + raw + "%";
        final net.momirealms.craftengine.core.plugin.context.text.StringTemplate dynamicTemplate =
                staticPlaceholder == null ? net.momirealms.craftengine.core.plugin.context.text.StringTemplate.of(raw) : null;
        final String fallback = args.length > 1 ? args[1] : null;
        return (boundArgs, context) -> {
            if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
                return null;
            }
            String placeholder = staticPlaceholder != null
                    ? staticPlaceholder
                    : "%" + dynamicTemplate.render(context) + "%";
            Player player = playerFor(context);
            String parsed = player != null
                    ? CraftEngine.instance().compatibilityManager().parse(player, placeholder)
                    : CraftEngine.instance().compatibilityManager().parse(null, placeholder);
            if (parsed.equals(placeholder)) {
                if (fallback == null) {
                    throw new IllegalArgumentException("No default papi value provided");
                }
                parsed = fallback;
            }
            return parsed;
        };
    }

    protected @Nullable Player playerFor(net.momirealms.craftengine.core.plugin.context.Context context) {
        return context instanceof PlayerContext playerContext ? playerContext.player() : null;
    }
}
