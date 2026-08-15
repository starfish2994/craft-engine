package net.momirealms.craftengine.bukkit.compatibility.nameplates;

import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.customnameplates.api.CustomNameplatesAPI;
import net.momirealms.customnameplates.api.feature.background.Background;
import net.momirealms.sparrow.message.Context;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BackgroundTag extends StaticTagResolver {
    public static final BackgroundTag INSTANCE = new BackgroundTag();

    private BackgroundTag() {
        super("background");
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        String id = arguments.popOr("No background id provided").toString();
        Optional<Background> background = CustomNameplatesAPI.getInstance().getBackground(id);
        if (background.isEmpty()) {
            return null;
        }
        double left = arguments.popOr("No argument left provided").asDouble().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));
        double right = arguments.popOr("No argument right provided").asDouble().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));
        String content = arguments.popOr("No argument content provided").toString();
        String textWithImage = CustomNameplatesAPI.getInstance().createTextWithImage(AdventureHelper.serializeMiniMessage(ctx.deserialize(content)), background.get(), (float) left, (float) right);
        return Tag.selfClosingInserting(ctx.deserialize(textWithImage));
    }

}
