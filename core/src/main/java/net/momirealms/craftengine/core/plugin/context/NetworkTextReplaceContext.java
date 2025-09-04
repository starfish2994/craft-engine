package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;

import java.util.Map;

public final class NetworkTextReplaceContext extends AbstractChainParameterContext {
    private final Player player;

    public NetworkTextReplaceContext(Player player) {
        super(new ContextHolder(Map.of(DirectContextParameters.PLAYER, () -> player)));
        this.player = player;
    }

    public static NetworkTextReplaceContext of(Player player) {
        return new NetworkTextReplaceContext(player);
    }

    public Player player() {
        return player;
    }

    @Override
    public TagResolver[] tagResolvers() {
        if (this.tagResolvers == null) {
            this.tagResolvers = new TagResolver[]{ShiftTag.INSTANCE, ImageTag.INSTANCE, new PlaceholderTag(this), new I18NTag(this),
                    new NamedArgumentTag(this), new ExpressionTag(this), new GlobalVariableTag(this)};
        }
        return this.tagResolvers;
    }
}
