package net.momirealms.craftengine.core.block;

public enum PushReaction {
    NORMAL,
    DESTROY,
    BLOCK,
    IGNORE,
    PUSH_ONLY;

    public static final PushReaction[] VALUES = values();
}
