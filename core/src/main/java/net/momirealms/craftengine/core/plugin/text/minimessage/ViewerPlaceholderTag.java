package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ViewerContext;
import org.jetbrains.annotations.Nullable;

public final class ViewerPlaceholderTag extends PlaceholderTag {
    public static final ViewerPlaceholderTag INSTANCE = new ViewerPlaceholderTag();

    private ViewerPlaceholderTag() {
        super("viewer_papi");
    }

    @Override
    protected @Nullable Player playerFor(net.momirealms.craftengine.core.plugin.context.Context context) {
        if (context instanceof ViewerContext viewerContext) {
            return viewerContext.viewer().player();
        }
        return null;
    }
}
