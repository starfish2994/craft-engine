package net.momirealms.craftengine.core.plugin.context;

import java.util.Optional;

public abstract class AbstractCommonContext implements Context {
    protected final ContextHolder contexts;

    public AbstractCommonContext(ContextHolder contexts) {
        this.contexts = contexts;
    }

    @Override
    public ContextHolder contexts() {
        return this.contexts;
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return this.contexts.getOptional(parameter);
    }
}
