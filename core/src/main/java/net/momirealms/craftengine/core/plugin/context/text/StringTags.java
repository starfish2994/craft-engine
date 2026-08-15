package net.momirealms.craftengine.core.plugin.context.text;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;
import net.momirealms.craftengine.core.plugin.text.minimessage.ExpressionTag;
import net.momirealms.craftengine.core.plugin.text.minimessage.RandomTag;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FastDecimalFormat;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StringTags {
    private static final Map<String, StringTag> TAGS = new HashMap<>();

    static {
        register("arg", (args, context) -> {
            String key = requireArg(args, 0, "No argument key provided");
            Object value = context.getOptionalParameter(ContextKey.chain(key)).orElse(null);
            if (value == null) {
                value = requireArg(args, 1, "No default value provided");
            }
            if (value instanceof Component component) {
                return AdventureHelper.plainTextContent(component);
            }
            return StringTemplates.render(String.valueOf(value), context);
        });
        register("viewer_arg", (args, context) -> {
            String key = requireArg(args, 0, "No argument key provided");
            Object value = context instanceof ViewerContext viewerContext
                    ? viewerContext.getViewerOptionalParameter(ContextKey.chain(key)).orElse(null)
                    : null;
            if (value == null) {
                value = requireArg(args, 1, "No default value provided");
            }
            if (value instanceof Component component) {
                return AdventureHelper.plainTextContent(component);
            }
            return StringTemplates.render(String.valueOf(value), context);
        });
        register("papi", (args, context) -> {
            if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
                return null;
            }
            String raw = requireArg(args, 0, "No argument placeholder provided");
            if (raw.contains("<")) {
                raw = StringTemplates.render(raw, context);
            }
            String placeholder = "%" + raw + "%";
            Player player = context instanceof PlayerContext playerContext ? playerContext.player() : null;
            String parsed = CraftEngine.instance().compatibilityManager().parse(player, placeholder);
            if (parsed.equals(placeholder)) {
                parsed = requireArg(args, 1, "No default papi value provided");
            }
            return parsed.contains("<") ? StringTemplates.render(parsed, context) : parsed;
        });
        register("viewer_papi", (args, context) -> {
            if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
                return null;
            }
            if (!(context instanceof ViewerContext viewerContext)) {
                return null;
            }
            String raw = requireArg(args, 0, "No argument placeholder provided");
            if (raw.contains("<")) {
                raw = StringTemplates.render(raw, context);
            }
            String placeholder = "%" + raw + "%";
            String parsed = CraftEngine.instance().compatibilityManager().parse(viewerContext.viewer().player(), placeholder);
            if (parsed.equals(placeholder)) {
                parsed = requireArg(args, 1, "No default papi value provided");
            }
            return parsed.contains("<") ? StringTemplates.render(parsed, context) : parsed;
        });
        register("rel_papi", (args, context) -> {
            if (!CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
                return null;
            }
            if (!(context instanceof ViewerContext viewerContext)) {
                return null;
            }
            Player p1 = viewerContext.owner() instanceof PlayerOptionalContext ownerContext ? ownerContext.player() : null;
            Player p2 = viewerContext.viewer().player();
            if (p1 == null || p2 == null) {
                return null;
            }
            String raw = requireArg(args, 0, "No argument relational placeholder provided");
            if (raw.contains("<")) {
                raw = StringTemplates.render(raw, context);
            }
            String placeholder = "%" + raw + "%";
            String parsed = CraftEngine.instance().compatibilityManager().parse(p1, p2, placeholder);
            if (parsed.equals(placeholder)) {
                parsed = requireArg(args, 1, "No default papi value provided");
            }
            return parsed.contains("<") ? StringTemplates.render(parsed, context) : parsed;
        });
        register("global", (args, context) -> {
            String id = requireArg(args, 0, "No argument variable id provided");
            String value = CraftEngine.instance().globalVariableManager().get(id);
            if (value == null) {
                throw new IllegalArgumentException("Unknown variable: " + id);
            }
            return StringTemplates.render(value, context);
        });
        register("random", (args, context) -> {
            String id = requireArg(args, 0, "No random id provided");
            final double value;
            if (args.length == 1) {
                value = ContextRandoms.getOrRoll(context, id);
            } else {
                List<String> params = Arrays.asList(args).subList(2, args.length);
                NumberProvider provider = RandomTag.getProvider(args[1], params);
                value = ContextRandoms.getOrRoll(context, id, () -> provider.getDouble(context));
            }
            return String.valueOf(value);
        });
        register("expr", (args, context) -> {
            String format = requireArg(args, 0, "No format provided");
            String rawExpression = requireArg(args, 1, "No expression provided");
            PrecompiledExpression compiled = ExpressionTag.CACHE.get(rawExpression, PrecompiledExpression::new);
            Number numberValue = compiled.evaluate(context).getNumberValue();
            if (format.equals("bool")) {
                return Boolean.toString(numberValue.doubleValue() != 0);
            }
            FastDecimalFormat df = ExpressionTag.FORMAT_CACHE.get(format, FastDecimalFormat::new);
            return df.format(numberValue.doubleValue());
        });
    }

    private StringTags() {
    }

    public static void register(String name, StringTag tag) {
        TAGS.put(name, tag);
    }

    public static boolean has(String name) {
        return TAGS.containsKey(name);
    }

    @Nullable
    public static StringTag get(String name) {
        return TAGS.get(name);
    }

    private static String requireArg(String[] args, int index, String message) {
        if (index >= args.length) {
            throw new IllegalArgumentException(message);
        }
        return args[index];
    }
}
