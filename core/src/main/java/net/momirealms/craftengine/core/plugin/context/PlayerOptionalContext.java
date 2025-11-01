package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PlayerOptionalContext extends AbstractChainParameterContext implements PlayerContext {
    /**
     * Use {@link #empty()} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlayerOptionalContext EMPTY = new PlayerOptionalContext(null, ContextHolder.empty());

    protected final Player player;

    public PlayerOptionalContext(@Nullable Player player,
                                 @NotNull ContextHolder contexts) {
        super(contexts);
        this.player = player;
    }

    public PlayerOptionalContext(@Nullable Player player,
                                 @NotNull ContextHolder contexts,
                                 List<AdditionalParameterProvider> additionalParameterProviders) {
        super(contexts, additionalParameterProviders);
        this.player = player;
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new PlayerOptionalContext(player, contexts);
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player, @NotNull ContextHolder.Builder contexts) {
        if (player != null) contexts.withParameter(DirectContextParameters.PLAYER, player);
        return new PlayerOptionalContext(player, contexts.build());
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player) {
        if (player == null) return empty();
        return new PlayerOptionalContext(player, new ContextHolder(Map.of(DirectContextParameters.PLAYER, () -> player)));
    }

    @NotNull
    public static PlayerOptionalContext empty() {
        return new PlayerOptionalContext(null, ContextHolder.empty());
    }

    @Override
    @Nullable
    public Player player() {
        return this.player;
    }

    public boolean isPlayerPresent() {
        return this.player != null;
    }
}
