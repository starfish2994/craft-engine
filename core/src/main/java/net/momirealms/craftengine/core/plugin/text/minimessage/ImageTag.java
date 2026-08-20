package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.font.AbstractFontManager;
import net.momirealms.craftengine.core.font.Image;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ImageTag extends StaticTagResolver {
    public static final ImageTag INSTANCE = new ImageTag();

    private ImageTag() { super("image"); }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String namespaceOrId = arguments.popOr("No argument namespace provided").toString();
        if (arguments.hasNext()) {
            String id = arguments.popOr("No argument id provided").toString();
            Optional<Image> optional = CraftEngine.instance().fontManager().imageById(Key.of(namespaceOrId, id));
            if (optional.isPresent()) {
                if (arguments.hasNext()) {
                    String rowOrFormat = arguments.popOr("No argument row provided").toString();
                    try {
                        int row = Integer.parseInt(rowOrFormat);
                        if (arguments.hasNext()) {
                            int column = arguments.popOr("No argument column provided").asInt().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));
                            if (arguments.hasNext()) {
                                String format = arguments.popOr("No argument format provided").toString();
                                return Tag.selfClosingInserting(ctx.deserialize(format + optional.get().miniMessageAt(row, column)));
                            } else {
                                return Tag.selfClosingInserting(optional.get().componentAt(row, column));
                            }
                        } else {
                            return Tag.selfClosingInserting(optional.get().componentAt(row, 0));
                        }
                    } catch (NumberFormatException e) {
                        return Tag.selfClosingInserting(ctx.deserialize(rowOrFormat + optional.get().miniMessageAt(0, 0)));
                    }
                } else {
                    return Tag.selfClosingInserting(optional.get().componentAt(0,0));
                }
            } else {
                throw ctx.newException("Invalid image id", arguments);
            }
        } else {
            Optional<Image> optional = ((AbstractFontManager) CraftEngine.instance().fontManager()).imageByIdValue(namespaceOrId);
            if (optional.isPresent()) {
                return Tag.selfClosingInserting(optional.get().componentAt(0, 0));
            } else {
                throw ctx.newException("Invalid image id", arguments);
            }
        }
    }

}
