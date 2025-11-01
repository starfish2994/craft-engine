package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class NetworkTextReplaceContext extends PlayerOptionalContext implements PlayerContext {

    public NetworkTextReplaceContext(Player player) {
        super(player, new ContextHolder(Map.of(DirectContextParameters.PLAYER, () -> player)));
    }

    public static @NotNull NetworkTextReplaceContext of(Player player) {
        return new NetworkTextReplaceContext(player);
    }

    @Override
    public Player player() {
        return super.player;
    }
}
