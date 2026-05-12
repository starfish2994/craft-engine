package net.momirealms.craftengine.proxy.common.text.minimessage;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.text.font.Image;
import net.momirealms.craftengine.proxy.common.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ImageTag implements TagResolver {
    private final NetworkTagData netWorkTagData;

    public ImageTag(NetworkTagData netWorkTagData) {
        this.netWorkTagData = netWorkTagData;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        String namespaceOrId = arguments.popOr("No argument namespace provided").toString();
        if (arguments.hasNext()) {
            String id = arguments.popOr("No argument id provided").toString();

            Image image = this.netWorkTagData.imageById(Key.of(namespaceOrId, id));
            if (image != null) {
                if (arguments.hasNext()) {
                    String rowOrFormat = arguments.popOr("No argument row provided").toString();
                    try {
                        int row = Integer.parseInt(rowOrFormat);
                        if (arguments.hasNext()) {
                            int column = arguments.popOr("No argument column provided").asInt().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));
                            if (arguments.hasNext()) {
                                String format = arguments.popOr("No argument format provided").toString();
                                return Tag.selfClosingInserting(ctx.deserialize(format + image.miniMessageAt(row, column)));
                            } else {
                                return Tag.selfClosingInserting(image.componentAt(row, column));
                            }
                        } else {
                            return Tag.selfClosingInserting(image.componentAt(row, 0));
                        }
                    } catch (NumberFormatException e) {
                        return Tag.selfClosingInserting(ctx.deserialize(rowOrFormat + image.miniMessageAt(0, 0)));
                    }
                } else {
                    return Tag.selfClosingInserting(image.componentAt(0,0));
                }
            } else {
                throw ctx.newException("Invalid image id", arguments);
            }
        } else {
            Image image = this.netWorkTagData.imageByIdValue(namespaceOrId);
            if (image != null) {
                return Tag.selfClosingInserting(image.componentAt(0, 0));
            } else {
                throw ctx.newException("Invalid image id", arguments);
            }
        }
    }

    @Override
    public boolean has(@NotNull String name) {
        return "image".equals(name);
    }
}
