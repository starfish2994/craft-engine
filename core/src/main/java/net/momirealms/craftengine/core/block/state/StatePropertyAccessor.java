package net.momirealms.craftengine.core.block.state;

import java.util.Collection;

public interface StatePropertyAccessor {

    String getPropertyValueAsString(String property);

    Collection<String> getPropertyNames();

    boolean hasProperty(String property);

    <T> T getPropertyValue(String property);
}
