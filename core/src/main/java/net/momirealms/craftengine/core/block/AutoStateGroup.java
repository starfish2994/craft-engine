package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public enum AutoStateGroup {
    LEAVES("leaves",
            Set.of(BlockKeys.OAK_LEAVES, BlockKeys.SPRUCE_LEAVES, BlockKeys.BIRCH_LEAVES, BlockKeys.JUNGLE_LEAVES, BlockKeys.ACACIA_LEAVES, BlockKeys.DARK_OAK_LEAVES, BlockKeys.MANGROVE_LEAVES, BlockKeys.CHERRY_LEAVES, BlockKeys.PALE_OAK_LEAVES, BlockKeys.AZALEA_LEAVES, BlockKeys.FLOWERING_AZALEA_LEAVES),
            (w) -> !(boolean) w.getProperty("waterlogged"), 0
    ),
    WATERLOGGED_LEAVES(
            "waterlogged_leaves",
            Set.of(BlockKeys.OAK_LEAVES, BlockKeys.SPRUCE_LEAVES, BlockKeys.BIRCH_LEAVES, BlockKeys.JUNGLE_LEAVES, BlockKeys.ACACIA_LEAVES, BlockKeys.DARK_OAK_LEAVES, BlockKeys.MANGROVE_LEAVES, BlockKeys.CHERRY_LEAVES, BlockKeys.PALE_OAK_LEAVES, BlockKeys.AZALEA_LEAVES, BlockKeys.FLOWERING_AZALEA_LEAVES),
            (w) -> w.getProperty("waterlogged"), 0
    ),
    TRIPWIRE("tripwire", Set.of(BlockKeys.TRIPWIRE), (w) -> true, 1),
    LOWER_TRIPWIRE("lower_tripwire", Set.of(BlockKeys.TRIPWIRE), (w) -> w.getProperty("attached"), 0),
    HIGHER_TRIPWIRE("higher_tripwire", Set.of(BlockKeys.TRIPWIRE), (w) -> !(boolean) w.getProperty("attached"), 0),
    NOTE_BLOCK("note_block", Set.of(BlockKeys.NOTE_BLOCK), (w) -> true, 0),
    BROWN_MUSHROOM("brown_mushroom", Set.of(BlockKeys.BROWN_MUSHROOM_BLOCK), (w) -> true, 0),
    RED_MUSHROOM("red_mushroom", Set.of(BlockKeys.RED_MUSHROOM_BLOCK), (w) -> true, 0),
    MUSHROOM_STEM("mushroom_stem", Set.of(BlockKeys.MUSHROOM_STEM), (w) -> true, 0),
    MUSHROOM("mushroom", Set.of(BlockKeys.BROWN_MUSHROOM_BLOCK, BlockKeys.RED_MUSHROOM_BLOCK, BlockKeys.MUSHROOM_STEM), (w) -> true, 1),
    SOLID("solid", Set.of(BlockKeys.BROWN_MUSHROOM_BLOCK, BlockKeys.RED_MUSHROOM_BLOCK, BlockKeys.MUSHROOM_STEM, BlockKeys.NOTE_BLOCK), (w) -> true, 2);

    private final Set<Key> blocks;
    private final String id;
    private final Predicate<BlockStateWrapper> predicate;
    private final int priority;

    AutoStateGroup(String id, Set<Key> blocks, Predicate<BlockStateWrapper> predicate, int priority) {
        this.id = id;
        this.blocks = blocks;
        this.predicate = predicate;
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public Set<Key> blocks() {
        return blocks;
    }

    public String id() {
        return id;
    }

    private static final Map<String, AutoStateGroup> BY_ID = new HashMap<>();
    private static final Map<Key, List<AutoStateGroup>> BY_BLOCKS = new HashMap<>();

    static {
        for (AutoStateGroup group : AutoStateGroup.values()) {
            BY_ID.put(group.id(), group);
            BY_ID.put(group.id().toUpperCase(Locale.ROOT), group);
            for (Key key : group.blocks) {
                BY_BLOCKS.computeIfAbsent(key, k -> new ArrayList<>(4)).add(group);
            }
        }
    }

    @Nullable
    public static AutoStateGroup byId(String id) {
        return BY_ID.get(id);
    }

    public static List<AutoStateGroup> findGroups(BlockStateWrapper wrapper) {
        return findGroups(wrapper.ownerId(), wrapper);
    }

    public static List<AutoStateGroup> findGroups(Key id, BlockStateWrapper wrapper) {
        List<AutoStateGroup> groups = BY_BLOCKS.get(id);
        if (groups == null) return Collections.emptyList();
        List<AutoStateGroup> result = new ArrayList<>(groups.size());
        for (AutoStateGroup group : groups) {
            if (group.predicate.test(wrapper)) result.add(group);
        }
        return result;
    }
}
