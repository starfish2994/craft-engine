package net.momirealms.craftengine.core.plugin.context.number;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.plugin.context.text.StringTags;
import net.momirealms.craftengine.core.util.ThrowableUtils;
import net.momirealms.sparrow.message.internal.parser.Token;
import net.momirealms.sparrow.message.internal.parser.TokenParser;
import net.momirealms.sparrow.message.internal.parser.TokenType;
import net.momirealms.sparrow.message.internal.parser.node.TagPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class PrecompiledExpression {
    private final String raw;
    private final Expression template;
    private final List<Snippet> snippets;

    public PrecompiledExpression(String expression) {
        this(expression, StringTags::has);
    }

    @SuppressWarnings("UnstableApiUsage")
    public PrecompiledExpression(String expression, Predicate<String> knownTag) {
        this.raw = expression;
        Expression template = null;
        List<Snippet> snippets = List.of();
        try {
            final StringBuilder substituted = new StringBuilder(expression.length());
            final List<Snippet> extracted = new ArrayList<>(2);
            for (final Token token : TokenParser.tokenize(expression, true)) {
                final TokenType type = token.type();
                if ((type == TokenType.OPEN_TAG || type == TokenType.OPEN_CLOSE_TAG) && !token.childTokens().isEmpty()) {
                    final List<Token> children = token.childTokens();
                    final String name = TokenParser.TagProvider.sanitizePlaceholderName(
                            children.getFirst().get(expression).toString());
                    if (knownTag.test(name)) {
                        final String[] args = new String[children.size() - 1];
                        for (int i = 1; i < children.size(); i++) {
                            final Token child = children.get(i);
                            args[i - 1] = TagPart.unquoteAndEscape(expression, child.startIndex(), child.endIndex());
                        }
                        // bind the tag handler and its pre-parsed arguments once and for all
                        extracted.add(new Snippet(
                                expression.substring(token.startIndex(), token.endIndex()),
                                StringTags.get(name),
                                args));
                        substituted.append("var").append(extracted.size());
                        continue;
                    }
                }
                substituted.append(expression, token.startIndex(), token.endIndex());
            }
            template = new Expression(substituted.toString());
            template.validate(); // force EvalEx compilation now, not on first use
            snippets = List.copyOf(extracted);
        } catch (final Throwable t) {
            ThrowableUtils.sneakyThrow(t);
        }
        this.template = template;
        this.snippets = snippets;
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

    public EvaluationValue evaluate() {
        try {
            return this.template.evaluate();
        } catch (final EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.raw, e);
        }
    }

    public EvaluationValue evaluate(Context context) {
        if (this.snippets.isEmpty()) {
            return this.evaluate();
        }
        try {
            final Expression instance = this.template.copy();
            for (int i = 0; i < this.snippets.size(); i++) {
                final Snippet snippet = this.snippets.get(i);
                String text = snippet.tag() != null ? snippet.tag().resolve(snippet.args(), context) : null;
                if (text == null) {
                    text = snippet.raw(); // unresolved tags stay literal, as in the string template engine
                }
                instance.with("var" + (i + 1), toValue(text));
            }
            return instance.evaluate();
        } catch (final EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.raw, e);
        }
    }

    private record Snippet(String raw, StringTag tag, String[] args) {
    }
}
