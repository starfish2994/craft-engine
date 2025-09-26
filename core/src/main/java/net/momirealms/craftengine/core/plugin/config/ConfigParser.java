package net.momirealms.craftengine.core.plugin.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigParser extends Comparable<ConfigParser> {

    String[] sectionId();

    int loadingSequence();

    @Override
    default int compareTo(@NotNull ConfigParser another) {
        return Integer.compare(loadingSequence(), another.loadingSequence());
    }

    default void postProcess() {
    }

    default void preProcess() {
    }
}
