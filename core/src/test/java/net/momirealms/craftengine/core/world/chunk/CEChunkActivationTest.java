package net.momirealms.craftengine.core.world.chunk;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldHeight;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CEChunkActivationTest {
    @Test
    void cachedChunkRetainsNeighborsQueriedDuringLoad() {
        assertNeighborsSurviveActivation(false);
    }

    @Test
    void deserializedChunkRetainsNeighborsQueriedDuringLoad() {
        assertNeighborsSurviveActivation(true);
    }

    private static void assertNeighborsSurviveActivation(boolean initiallyValid) {
        TestChunk chunk = new TestChunk();
        BlockEntity first = BlockEntity.inactive(new BlockPos(0, 0, 0), null, new CompoundTag());
        BlockEntity second = BlockEntity.inactive(new BlockPos(1, 0, 0), null, new CompoundTag());
        List<BlockEntity> firstLookups = new ArrayList<>();
        List<BlockEntity> secondLookups = new ArrayList<>();
        first.controller = queryingController(first, second, chunk, firstLookups);
        second.controller = queryingController(second, first, chunk, secondLookups);
        // Cached chunks retain the invalid entities left by deactivateAllBlockEntities;
        // deserialization instead marks every entity valid before activation begins.
        chunk.restore(first, initiallyValid);
        chunk.restore(second, initiallyValid);

        chunk.activateAllBlockEntities();
        chunk.activateAllBlockEntities();

        assertEquals(2, chunk.blockEntities().size(), "A neighbor lookup must not remove a cached entity");
        assertSame(first, chunk.getBlockEntity(first.pos, false));
        assertSame(second, chunk.getBlockEntity(second.pos, false));
        assertEquals(List.of(second), firstLookups, "onLoad must see the original neighbor exactly once");
        assertEquals(List.of(first), secondLookups, "onLoad must see the original neighbor exactly once");
        assertTrue(first.isValid());
        assertTrue(second.isValid());
    }

    private static BlockEntityController queryingController(BlockEntity entity, BlockEntity neighbor,
                                                            TestChunk chunk, List<BlockEntity> lookups) {
        return new BlockEntityController(entity) {
            @Override
            public void onLoad() {
                // Pipe onLoad queries adjacent entities through this same create-enabled lookup.
                lookups.add(chunk.getBlockEntity(neighbor.pos, true));
            }
        };
    }

    private static CEWorld testWorld() {
        World world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("worldHeight")) return WorldHeight.create(0, 0);
                    throw new UnsupportedOperationException(method.getName());
                });
        WorldDataStorage storage = (WorldDataStorage) Proxy.newProxyInstance(WorldDataStorage.class.getClassLoader(),
                new Class<?>[]{WorldDataStorage.class}, (proxy, method, args) -> {
                    if (method.getName().equals("readSettings")) return null;
                    throw new UnsupportedOperationException(method.getName());
                });
        return new CEWorld(world, storage) {
            @Override
            public void updateLight() {
            }
        };
    }

    private static final class TestChunk extends CEChunk {
        private TestChunk() {
            super(testWorld(), new ChunkPos(0, 0));
        }

        private void restore(BlockEntity entity, boolean valid) {
            entity.setWorld(this.world);
            entity.setValid(valid);
            this.blockEntities.put(entity.pos.asLong(), entity);
        }

        @Override
        public void replaceOrCreateTickingBlockEntity(BlockEntity entity) {
        }

        @Override
        public <T extends BlockEntity> void createDynamicBlockEntityRenderer(T entity) {
        }
    }
}
