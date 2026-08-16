package net.momirealms.craftengine.core.plugin.context;

import java.util.Optional;

public abstract class AbstractChainParameterContext implements Context {
    protected final ContextHolder contexts;

    public AbstractChainParameterContext(ContextHolder contexts) {
        this.contexts = contexts;
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        ContextKey<Object> parentKey = parameter.parent();
        if (parentKey == null) {
            return this.contexts.getOptional(parameter);
        }
        Optional<Object> parentValue = getOptionalParameter(parentKey);
        if (parentValue.isEmpty()) {
            return Optional.empty();
        }
        if (parentValue.get() instanceof ChainParameterSource source) {
            return source.getParameter(parameter);
        }
        return Optional.empty();
    }

    @Override
    public ContextHolder contexts() {
        return this.contexts;
    }
}
