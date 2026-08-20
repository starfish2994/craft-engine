package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import net.momirealms.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public final class PlainL10NTag extends StaticTagResolver {
    public static final TagResolver INSTANCE = new PlainL10NTag();

    private PlainL10NTag() { super("l10n"); }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue aq, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        String l10nKey = aq.popOr("No argument l10n key provided").toString();
        if (aq.hasNext()) {
            StringJoiner joiner = new StringJoiner(":");
            while (aq.hasNext()) {
                Tag.Argument arg = aq.pop();
                joiner.add("'" + AdventureHelper.serializeMiniMessage(ctx.deserialize(arg.value())) + "'");
            }
            return Tag.selfClosingInserting(Component.text("<l10n:" + l10nKey + ":" + joiner + ">"));
        } else {
            return Tag.selfClosingInserting(Component.text("<l10n:" + l10nKey + ">"));
        }
    }
}
