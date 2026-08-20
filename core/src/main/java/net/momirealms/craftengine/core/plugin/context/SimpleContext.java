package net.momirealms.craftengine.core.plugin.context;

public class SimpleContext extends AbstractChainParameterContext {
    public static final SimpleContext EMPTY = new SimpleContext(ContextHolder.empty());

    public SimpleContext(ContextHolder contexts) {
        super(contexts);
    }

    public static SimpleContext of(ContextHolder contexts) {
        return new SimpleContext(contexts);
    }
}
