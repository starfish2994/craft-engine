package net.momirealms.craftengine.core.pack.conflict;

import net.momirealms.craftengine.core.plugin.context.AbstractCommonContext;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;

import java.nio.file.Path;

public class PathContext extends AbstractCommonContext {
    private final Path path;

    public PathContext(ContextHolder holder, Path path) {
        super(holder);
        this.path = path;
    }

    public Path path() {
        return path;
    }

    public static PathContext of(Path path) {
        return new PathContext(ContextHolder.empty(), path);
    }
}
