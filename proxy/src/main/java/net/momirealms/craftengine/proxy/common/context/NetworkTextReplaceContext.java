package net.momirealms.craftengine.proxy.common.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.text.minimessage.NetworkL10NTag;
import net.momirealms.craftengine.proxy.common.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public final class NetworkTextReplaceContext implements Context {
    private final ProxyPlayer player;
    private final TagResolver[] staticTagResolvers;
    private final NetworkTagData netWorkTagData;
    private TagResolver[] tagResolvers;

    public NetworkTextReplaceContext(ProxyPlayer player, NetworkTagData netWorkTagData) {
        this.player = player;
        this.netWorkTagData = netWorkTagData;
        this.staticTagResolvers = netWorkTagData.tagResolvers();
    }

    @NotNull
    public ProxyPlayer player() {
        return this.player;
    }

    @NotNull
    public NetworkTagData tagData() {
        return this.netWorkTagData;
    }

    @Override
    public TagResolver[] tagResolvers() {
        if (this.tagResolvers == null) {
            this.tagResolvers = ArrayUtils.appendElementToArrayTail(this.staticTagResolvers, new NetworkL10NTag(this));
        }
        return this.tagResolvers;
    }
}
