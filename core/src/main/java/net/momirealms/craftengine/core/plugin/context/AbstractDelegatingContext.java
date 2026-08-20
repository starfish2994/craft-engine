package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.util.random.RandomSource;

import java.util.Optional;

public abstract class AbstractDelegatingContext implements Context {
    protected final Context delegate;

    public AbstractDelegatingContext(Context delegate) {
        this.delegate = delegate;
    }

    @Override
    public RandomSource random() {
        return this.delegate.random();
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return this.delegate.getOptionalParameter(parameter);
    }

    @Override
    public ContextHolder contexts() {
        return this.delegate.contexts();
    }
}
