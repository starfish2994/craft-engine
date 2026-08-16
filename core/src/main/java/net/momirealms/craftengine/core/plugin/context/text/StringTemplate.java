package net.momirealms.craftengine.core.plugin.context.text;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.sparrow.message.internal.parser.Token;
import net.momirealms.sparrow.message.internal.parser.TokenParser;
import net.momirealms.sparrow.message.internal.parser.TokenType;
import net.momirealms.sparrow.message.internal.parser.node.TagPart;

import java.util.ArrayList;
import java.util.List;

public final class StringTemplate {
    private final String raw;
    private final Object[] parts;
    private final boolean hasTags;

    private StringTemplate(String raw, Object[] parts, boolean hasTags) {
        this.raw = raw;
        this.parts = parts;
        this.hasTags = hasTags;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static StringTemplate of(String raw) {
        final List<Object> parts = new ArrayList<>();
        boolean hasTags = false;
        for (final Token token : TokenParser.tokenize(raw, true)) {
            final TokenType type = token.type();
            if ((type == TokenType.OPEN_TAG || type == TokenType.OPEN_CLOSE_TAG) && !token.childTokens().isEmpty()) {
                final List<Token> children = token.childTokens();
                final String name = TokenParser.TagProvider.sanitizePlaceholderName(children.getFirst().get(raw).toString());
                final StringTag tag = StringTags.get(name);
                if (tag != null) {
                    final String[] args = new String[children.size() - 1];
                    for (int i = 1; i < children.size(); i++) {
                        final Token child = children.get(i);
                        args[i - 1] = TagPart.unquoteAndEscape(raw, child.startIndex(), child.endIndex());
                    }
                    parts.add(new TagCall(raw.substring(token.startIndex(), token.endIndex()), tag.precompile(args), args));
                    hasTags = true;
                    continue;
                }
            }
            parts.add(raw.substring(token.startIndex(), token.endIndex()));
        }
        return new StringTemplate(raw, parts.toArray(), hasTags);
    }

    public boolean hasTags() {
        return this.hasTags;
    }

    public String render(Context context) {
        if (!this.hasTags) {
            return this.raw;
        }
        final StringBuilder sb = new StringBuilder(this.raw.length() + 16);
        for (final Object part : this.parts) {
            if (part instanceof String literal) {
                sb.append(literal);
                continue;
            }
            final TagCall call = (TagCall) part;
            final Object value;
            try {
                value = call.tag.resolve(call.args, context);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to resolve string tag " + call.raw, e);
            }
            sb.append(value == null ? call.raw : value);
        }
        return sb.toString();
    }

    private record TagCall(String raw, StringTag tag, String[] args) {
    }
}
