package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NamedArgumentTag extends StaticTagResolver {
    public static final NamedArgumentTag INSTANCE = new NamedArgumentTag("arg");

    protected NamedArgumentTag(String name) {
        super(name);
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        if (!(ctx.target() instanceof net.momirealms.craftengine.core.plugin.context.Context context)) {
            return null;
        }
        ContextKey<?> key = ContextKey.chain(arguments.popOr("No argument key provided").toString());
        Optional<?> optional = parameter(context, key);
        Object value = optional.orElse(null);
        if (value == null) {
            value = arguments.popOr("No default value provided").toString();
        }
        if (value instanceof Component component) {
            return Tag.selfClosingInserting(component);
        }
        return Tag.selfClosingInserting(ctx.deserialize(String.valueOf(value)));
    }

    protected Optional<?> parameter(net.momirealms.craftengine.core.plugin.context.Context context, ContextKey<?> key) {
        return context.getOptionalParameter(key);
    }
}
