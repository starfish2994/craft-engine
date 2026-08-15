package net.momirealms.craftengine.core.plugin.context.number;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.text.StringTags;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.plugin.context.text.StringTags;
import net.momirealms.craftengine.core.plugin.context.text.StringTemplates;
import net.momirealms.craftengine.core.util.ThrowableUtils;
import net.momirealms.sparrow.message.internal.parser.Token;
import net.momirealms.sparrow.message.internal.parser.TokenParser;
import net.momirealms.sparrow.message.internal.parser.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PrecompiledExpression {
    private final String raw;
    private final Expression template;
    private final List<String> tagSnippets;

    public PrecompiledExpression(String expression) {
        this(expression, StringTags::has);
    }

    @SuppressWarnings("UnstableApiUsage")
    public PrecompiledExpression(String expression, Predicate<String> knownTag) {
        this.raw = expression;
        Expression template = null;
        List<String> snippets = List.of();
        try {
            final StringBuilder substituted = new StringBuilder(expression.length());
            final List<String> extracted = new ArrayList<>(2);
            for (final Token token : TokenParser.tokenize(expression, true)) {
                final TokenType type = token.type();
                if ((type == TokenType.OPEN_TAG || type == TokenType.OPEN_CLOSE_TAG) && !token.childTokens().isEmpty()) {
                    final String name = TokenParser.TagProvider.sanitizePlaceholderName(
                            token.childTokens().getFirst().get(expression).toString());
                    if (knownTag.test(name)) {
                        extracted.add(expression.substring(token.startIndex(), token.endIndex()));
                        substituted.append("var").append(extracted.size());
                        continue;
                    }
                }
                substituted.append(expression, token.startIndex(), token.endIndex());
            }
            template = new Expression(substituted.toString());
            template.validate();
            snippets = List.copyOf(extracted);
        } catch (final Throwable t) {
            ThrowableUtils.sneakyThrow(t);
        }
        this.template = template;
        this.tagSnippets = snippets;
    }

    public EvaluationValue evaluate() {
        try {
            return this.template.evaluate();
        } catch (final EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.raw, e);
        }
    }

    public boolean hasTags() {
        return !this.tagSnippets.isEmpty();
    }

    public EvaluationValue evaluate(Context context) {
        return evaluate(snippet -> StringTemplates.render(snippet, context));
    }

    public EvaluationValue evaluate(Function<String, String> snippetEvaluator) {
        try {
            if (this.tagSnippets.isEmpty()) {
                return this.template.evaluate();
            }
            final Expression instance = this.template.copy(); // shares the parsed AST, fresh bindings
            for (int i = 0; i < this.tagSnippets.size(); i++) {
                instance.with("var" + (i + 1), toValue(snippetEvaluator.apply(this.tagSnippets.get(i))));
            }
            return instance.evaluate();
        } catch (final EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.raw, e);
        }
    }

    private static Object toValue(String text) {
        try {
            return Double.parseDouble(text);
        } catch (final NumberFormatException e) {
            return switch (text) {
                case "true", "yes", "TRUE", "YES" -> true;
                case "false", "no", "FALSE", "NO" -> false;
                case "null", "NULL" -> null;
                default -> text;
            };
        }
    }
}
