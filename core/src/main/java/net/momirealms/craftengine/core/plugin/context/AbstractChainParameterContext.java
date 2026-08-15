package net.momirealms.craftengine.core.plugin.context;

import java.util.Optional;

public abstract class AbstractChainParameterContext extends AbstractCommonContext {

    public AbstractChainParameterContext(ContextHolder contexts) {
        super(contexts);
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        ContextKey<Object> parentKey = parameter.parent();
        if (parentKey == null) {
            return super.getOptionalParameter(parameter);
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
}
