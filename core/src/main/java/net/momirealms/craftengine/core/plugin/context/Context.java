package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.pointer.Pointered;

import java.util.Optional;

public interface Context extends Pointered {

    ContextHolder contexts();

    <T> Optional<T> getOptionalParameter(ContextKey<T> parameter);

    default <T> T getParameterOrThrow(ContextKey<T> parameter) {
        return getOptionalParameter(parameter).orElseThrow(() -> new RuntimeException("No parameter found for " + parameter));
    }
}
