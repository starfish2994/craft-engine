package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.plugin.context.text.StringTemplates;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GlobalVariableTag extends StaticTagResolver implements StringTag {
    public static final GlobalVariableTag INSTANCE = new GlobalVariableTag();

    private GlobalVariableTag() {
        super("global");
    }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        String id = arguments.popOr("No argument variable id provided").toString();
        String value = CraftEngine.instance().globalVariableManager().get(id);
        if (value == null) {
            throw ctx.newException("Unknown variable: " + id, arguments);
        }
        if (!arguments.hasNext()) {
            return Tag.selfClosingInserting(ctx.deserialize(value));
        } else {
            List<Component> args = new ArrayList<>();
            while (arguments.hasNext()) {
                args.add(ctx.deserialize(arguments.popOr("No index argument variable id provided").toString()));
            }
            return Tag.selfClosingInserting(ctx.deserialize(value, new IndexedArgumentTag(args)));
        }
    }

    @Override
    public @Nullable String resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        String id = StringTag.requireArg(args, 0, "No argument variable id provided");
        String value = CraftEngine.instance().globalVariableManager().get(id);
        if (value == null) {
            throw new IllegalArgumentException("Unknown variable: " + id);
        }
        return StringTemplates.render(value, context);
    }
}
