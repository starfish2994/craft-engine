package net.momirealms.craftengine.core.plugin.context;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCommonContext implements Context {
    protected final ContextHolder contexts;
    protected final List<AdditionalParameterProvider> additionalParameterProviders;

    public AbstractCommonContext(ContextHolder contexts) {
        this.contexts = contexts;
        this.additionalParameterProviders = List.of(new CommonParameterProvider());
    }

    public AbstractCommonContext(ContextHolder contexts,
                                 List<AdditionalParameterProvider> additionalParameterProviders) {
        this.contexts = contexts;
        this.additionalParameterProviders = additionalParameterProviders;
    }

    @Override
    public ContextHolder contexts() {
        return this.contexts;
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        if (!this.additionalParameterProviders.isEmpty()) {
            for (AdditionalParameterProvider additionalParameterProvider : additionalParameterProviders) {
                Optional<T> optionalValue = additionalParameterProvider.getOptionalParameter(parameter);
                if (optionalValue.isPresent()) {
                    return optionalValue;
                }
            }
        }
        return this.contexts.getOptional(parameter);
    }
}
