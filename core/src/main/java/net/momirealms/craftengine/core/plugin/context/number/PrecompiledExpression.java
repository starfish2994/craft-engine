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

import java.util.*;
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
                                StringTags.get(name) != null ? Objects.requireNonNull(StringTags.get(name)).precompile(args) : null,
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

    private static Object toValue(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        String formatted = value.toString();
        try {
            return Double.parseDouble(formatted);
        } catch (final NumberFormatException e) {
            return switch (formatted) {
                case "true", "yes", "TRUE", "YES" -> true;
                case "false", "no", "FALSE", "NO" -> false;
                case "null", "NULL" -> null;
                default -> formatted;
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
        return this.evaluate(context, Map.of());
    }

    public EvaluationValue evaluate(Context context, Map<String, ?> extraVariables) {
        if (this.snippets.isEmpty() && extraVariables.isEmpty()) {
            return this.evaluate();
        }
        return evaluateBound(context, extraVariables, this.copyTemplate());
    }

    private EvaluationValue evaluateBound(Context context, Map<String, ?> extraVariables, Expression instance) {
        try {
            for (final Map.Entry<String, ?> entry : extraVariables.entrySet()) {
                instance.with(entry.getKey(), entry.getValue());
            }
            for (int i = 0; i < this.snippets.size(); i++) {
                final Snippet snippet = this.snippets.get(i);
                Object value = snippet.tag() != null ? snippet.tag().resolve(snippet.args(), context) : null;
                if (value == null) {
                    value = snippet.raw(); // unresolved tags stay literal, as in the string template engine
                }
                instance.with("var" + (i + 1), toValue(value));
            }
            return instance.evaluate();
        } catch (final EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.raw, e);
        }
    }

    private Expression copyTemplate() {
        try {
            return this.template.copy();
        } catch (final ParseException e) {
            throw new IllegalStateException("Validated expression could not be copied: " + this.raw, e);
        }
    }

    public Set<String> usedVariables() {
        try {
            final Set<String> used = new HashSet<>(this.template.getUsedVariables());
            for (int i = 1; i <= this.snippets.size(); i++) {
                used.remove("var" + i);
            }
            return used;
        } catch (final ParseException e) {
            return Set.of();
        }
    }

    public String raw() {
        return this.raw;
    }

    private record Snippet(String raw, StringTag tag, String[] args) {
    }
}
