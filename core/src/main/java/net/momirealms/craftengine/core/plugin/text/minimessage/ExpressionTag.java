package net.momirealms.craftengine.core.plugin.text.minimessage;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import net.momirealms.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class ExpressionTag extends StaticTagResolver {
    public static final TagResolver INSTANCE = new ExpressionTag();

    private ExpressionTag() { super("expr"); }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String format = arguments.popOr("No format provided").toString();
        String expr = arguments.popOr("No expression provided").toString();

        Component resultComponent = ctx.deserialize(expr);
        String resultString = AdventureHelper.plainTextContent(resultComponent);
        Expression expression = new Expression(resultString);

        try {
            Number numberValue = expression.evaluate().getNumberValue();
            DecimalFormat df = new DecimalFormat(format);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            df.setDecimalFormatSymbols(symbols);
            String formatted = df.format(numberValue);
            return Tag.selfClosingInserting(Component.text(formatted));
        } catch (IllegalArgumentException e) {
            throw ctx.newException("Invalid number format: " + format, arguments);
        } catch (EvaluationException | ParseException e) {
            throw ctx.newException("Invalid expression: " + e.getMessage(), arguments);
        }
    }
}