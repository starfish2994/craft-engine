package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.RelationalContext;

import java.util.Optional;

public final class ViewerNamedArgumentTag extends NamedArgumentTag {
    public static final ViewerNamedArgumentTag INSTANCE = new ViewerNamedArgumentTag();

    private ViewerNamedArgumentTag() {
        super("viewer_arg");
    }

    @Override
    protected Optional<?> parameter(net.momirealms.craftengine.core.plugin.context.Context context, ContextKey<?> key) {
        if (context instanceof RelationalContext relationalContext) {
            return relationalContext.getViewerOptionalParameter(key);
        }
        return Optional.empty();
    }
}
