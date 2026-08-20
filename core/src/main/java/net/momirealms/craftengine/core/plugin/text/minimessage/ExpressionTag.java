package net.momirealms.craftengine.core.plugin.text.minimessage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.util.FastDecimalFormat;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class ExpressionTag extends StaticTagResolver implements StringTag {
    public static final ExpressionTag INSTANCE = new ExpressionTag();
    public static final Cache<String, PrecompiledExpression> CACHE = Caffeine.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    public static final Cache<String, FastDecimalFormat> FORMAT_CACHE = Caffeine.newBuilder()
            .maximumSize(64)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private ExpressionTag() {
        super("expr");
    }

    public static void clearCaches() {
        CACHE.invalidateAll();
        FORMAT_CACHE.invalidateAll();
    }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String format = arguments.popOr("No format provided").toString();
        String expr = arguments.popOr("No expression provided").toString();

        PrecompiledExpression compiled = CACHE.get(expr, PrecompiledExpression::new);
        final Number numberValue;
        try {
            if (ctx.target() instanceof net.momirealms.craftengine.core.plugin.context.Context context) {
                numberValue = compiled.evaluate(context).getNumberValue();
            } else {
                numberValue = compiled.evaluate().getNumberValue();
            }
        } catch (final RuntimeException e) {
            throw ctx.newException("Invalid expression: " + expr, e, arguments);
        }
        if (format.equals("bool")) {
            return Tag.selfClosingInserting(Component.text(Boolean.toString(numberValue.doubleValue() != 0)));
        }
        final FastDecimalFormat df;
        try {
            df = FORMAT_CACHE.get(format, FastDecimalFormat::new);
        } catch (final IllegalArgumentException e) {
            throw ctx.newException("Invalid number format: " + format, arguments);
        }
        return Tag.selfClosingInserting(Component.text(df.format(numberValue.doubleValue())));
    }

    @Override
    public String resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        String format = StringTag.requireArg(args, 0, "No format provided");
        String expr = StringTag.requireArg(args, 1, "No expression provided");
        PrecompiledExpression compiled = CACHE.get(expr, PrecompiledExpression::new);
        Number numberValue = (context != null ? compiled.evaluate(context) : compiled.evaluate()).getNumberValue();
        if (format.equals("bool")) {
            return Boolean.toString(numberValue.doubleValue() != 0);
        }
        return FORMAT_CACHE.get(format, FastDecimalFormat::new).format(numberValue.doubleValue());
    }

    @Override
    public StringTag precompile(String[] args) {
        final String format = StringTag.requireArg(args, 0, "No format provided");
        final String rawExpression = StringTag.requireArg(args, 1, "No expression provided");
        final PrecompiledExpression compiled = CACHE.get(rawExpression, PrecompiledExpression::new);
        if (format.equals("bool")) {
            return (boundArgs, context) -> {
                Number numberValue = (context != null ? compiled.evaluate(context) : compiled.evaluate()).getNumberValue();
                return numberValue.doubleValue() != 0;
            };
        }
        final FastDecimalFormat df = FORMAT_CACHE.get(format, FastDecimalFormat::new);
        return (boundArgs, context) -> {
            Number numberValue = (context != null ? compiled.evaluate(context) : compiled.evaluate()).getNumberValue();
            return df.format(numberValue.doubleValue());
        };
    }
}
