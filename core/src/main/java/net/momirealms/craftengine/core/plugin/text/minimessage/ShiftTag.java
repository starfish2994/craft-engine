package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;

public final class ShiftTag extends StaticTagResolver {
    public static final ShiftTag INSTANCE = new ShiftTag();

    private ShiftTag() { super("shift"); }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String shiftAmount = arguments.popOr("No argument shift provided").toString();
        try {
            int shift = Integer.parseInt(shiftAmount);
            return Tag.selfClosingInserting(AdventureHelper.miniMessage().deserialize(CraftEngine.instance().fontManager().createMiniMessageOffsets(shift)));
        } catch (NumberFormatException e) {
            throw ctx.newException("Invalid shift value", arguments);
        }
    }
}
