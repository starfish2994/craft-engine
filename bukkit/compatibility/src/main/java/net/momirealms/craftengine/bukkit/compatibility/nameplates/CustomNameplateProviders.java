package net.momirealms.craftengine.bukkit.compatibility.nameplates;

import net.momirealms.craftengine.core.plugin.compatibility.TagResolverProvider;
import net.momirealms.sparrow.message.tag.resolver.TagResolver;

public final class CustomNameplateProviders {

    public static class Background implements TagResolverProvider {
        @Override
        public String name() {
            return "background";
        }

        @Override
        public TagResolver getTagResolver() {
            return BackgroundTag.INSTANCE;
        }
    }

    public static class Nameplate implements TagResolverProvider {
        @Override
        public String name() {
            return "nameplate";
        }

        @Override
        public TagResolver getTagResolver() {
            return NameplateTag.INSTANCE;
        }
    }

    public static class Bubble implements TagResolverProvider {
        @Override
        public String name() {
            return "bubble";
        }

        @Override
        public TagResolver getTagResolver() {
            return BubbleTag.INSTANCE;
        }
    }
}
