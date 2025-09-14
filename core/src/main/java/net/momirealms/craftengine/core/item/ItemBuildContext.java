package net.momirealms.craftengine.core.item;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemBuildContext extends PlayerOptionalContext {

    /**
     * Use {@link #empty()} instead
     */
    @Deprecated(since = "0.0.63", forRemoval = true)
    public static final ItemBuildContext EMPTY = new ItemBuildContext(null, ContextHolder.empty());
    public static final TagResolver[] EMPTY_RESOLVERS = empty().tagResolvers();

    public ItemBuildContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(player, contexts);
    }

    @NotNull
    public static ItemBuildContext empty() {
        return new ItemBuildContext(null, ContextHolder.empty());
    }

    @NotNull
    public static ItemBuildContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new ItemBuildContext(player, contexts);
    }

    @NotNull
    public static ItemBuildContext of(@Nullable Player player, @NotNull ContextHolder.Builder builder) {
        if (player != null) builder.withParameter(DirectContextParameters.PLAYER, player);
        return new ItemBuildContext(player, builder.build());
    }

    @NotNull
    public static ItemBuildContext of(@Nullable Player player) {
        if (player == null) return new ItemBuildContext(null, ContextHolder.empty());
        return new ItemBuildContext(player, new ContextHolder(Map.of(DirectContextParameters.PLAYER, () -> player)));
    }
}
