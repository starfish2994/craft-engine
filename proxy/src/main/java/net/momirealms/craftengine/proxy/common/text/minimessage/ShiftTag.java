package net.momirealms.craftengine.proxy.common.text.minimessage;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.text.font.OffsetFont;
import net.momirealms.craftengine.proxy.common.util.FormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShiftTag implements TagResolver {
    private final OffsetFont offsetFont;

    public ShiftTag(NetworkTagData netWorkTagData) {
        this.offsetFont = netWorkTagData.offset();
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        String shiftAmount = arguments.popOr("No argument shift provided").toString();
        try {
            int shift = Integer.parseInt(shiftAmount);
            return Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(this.createMiniMessageOffsets(shift)));
        } catch (NumberFormatException e) {
            throw ctx.newException("Invalid shift value", arguments);
        }
    }

    @Override
    public boolean has(@NotNull String name) {
        return "shift".equals(name);
    }

    private String createMiniMessageOffsets(int shift) {
        return this.offsetFont.createOffset(shift, FormatUtils::miniMessageFont);
    }
}
