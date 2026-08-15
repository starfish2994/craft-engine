package net.momirealms.craftengine.core.plugin.text.minimessage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FastDecimalFormat;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import net.momirealms.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class ExpressionTag extends StaticTagResolver {
    public static final TagResolver INSTANCE = new ExpressionTag();
    private static final Cache<String, PrecompiledExpression> CACHE = Caffeine.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private static final Cache<String, FastDecimalFormat> FORMAT_CACHE = Caffeine.newBuilder()
            .maximumSize(64)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private ExpressionTag() {
        super("expr");
    }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String format = arguments.popOr("No format provided").toString();
        String expr = arguments.popOr("No expression provided").toString();

        PrecompiledExpression compiled = CACHE.get(expr, PrecompiledExpression::new);
        final Number numberValue;
        try {
            numberValue = compiled.evaluate(snippet -> AdventureHelper.plainTextContent(ctx.deserialize(snippet))).getNumberValue();
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
}
