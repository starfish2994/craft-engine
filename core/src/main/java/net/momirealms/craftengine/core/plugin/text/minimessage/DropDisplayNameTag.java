package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DropDisplayNameTag extends StaticTagResolver {
    public static final DropDisplayNameTag INSTANCE = new DropDisplayNameTag();

    private DropDisplayNameTag() {
        super("name");
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        if (!(ctx.target() instanceof net.momirealms.craftengine.core.plugin.context.Context context)) {
            return null;
        }
        Component component = context.getOptionalParameter(DirectContextParameters.HOVER_COMPONENT).orElse(null);
        if (component == null) {
            return null;
        }
        return Tag.selfClosingInserting(component);
    }
}
