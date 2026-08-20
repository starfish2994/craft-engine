package net.momirealms.craftengine.core.plugin.compatibility;

import net.momirealms.sparrow.message.tag.resolver.TagResolver;

/**
 * Provides external tag resolvers. Implementations should prefer stateless singletons
 * that read contextual data from the parse target ({@code ctx.target()}); the
 * {@code context} argument is kept for backward compatibility.
 */
public interface TagResolverProvider {

    String name();

    TagResolver getTagResolver();
}
