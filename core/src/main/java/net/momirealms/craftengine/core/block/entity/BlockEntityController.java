package net.momirealms.craftengine.core.block.entity;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BlockEntityController {
    protected final BlockEntity blockEntity;

    public BlockEntityController(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public BlockEntity blockEntity() {
        return this.blockEntity;
    }

    @SuppressWarnings("unchecked")
    public <C extends BlockEntityController> void let(Class<C> clazz, int index, Consumer<C> consumer) {
        if (clazz.isInstance(this)) {
            consumer.accept((C) this);
        }
    }

    @SuppressWarnings("unchecked")
    public <C extends BlockEntityController, V> V let(Class<C> clazz, int index, Function<C, V> function) {
        if (clazz.isInstance(this)) {
            return function.apply((C) this);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <C extends BlockEntityController> C get(Class<C> clazz, int index) {
        if (clazz.isInstance(this)) {
            return (C) this;
        }
        return null;
    }

    public boolean hasElement() {
        return false;
    }

    public void gatherElements(Consumer<BlockEntityElement> consumer) {
    }

    public void saveCustomData(CompoundTag data) {
    }

    public void loadCustomData(CompoundTag data) {
    }

    public void onRemove() {
    }

    public void onBlockStateChange(ImmutableBlockState state) {
    }

    public <C extends BlockEntityController> BlockEntityTicker<C> createBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
        return null;
    }

    public <C extends BlockEntityController> BlockEntityTicker<C> createAsyncBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <C extends BlockEntityController, T extends BlockEntityController> BlockEntityTicker<C> createTickerHelper(BlockEntityTicker<? super T> ticker) {
        return (BlockEntityTicker<C>) ticker;
    }

    @Nullable
    public static BlockEntityController createFromList(BlockEntity blockEntity, List<BlockEntityController> list) {
        return switch (list.size()) {
            case 0 -> null;
            case 1 -> list.getFirst();
            case 2 -> new BiController(blockEntity, list.get(0), list.get(1));
            default -> new CompositeController(blockEntity, list.toArray(new BlockEntityController[0]));
        };
    }

    private static final class BiController extends BlockEntityController {
        private final BlockEntityController first;
        private final BlockEntityController second;

        private BiController(BlockEntity blockEntity, BlockEntityController first, BlockEntityController second) {
            super(blockEntity);
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean hasElement() {
            return this.first.hasElement() || this.second.hasElement();
        }

        @Override
        public void gatherElements(Consumer<BlockEntityElement> consumer) {
            this.first.gatherElements(consumer);
            this.second.gatherElements(consumer);
        }

        @Override
        public <C extends BlockEntityController> BlockEntityTicker<C> createBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
            BlockEntityTicker<BlockEntityController> t1 = this.first.createBlockEntityTicker(world, blockState);
            BlockEntityTicker<BlockEntityController> t2 = this.second.createBlockEntityTicker(world, blockState);
            return createTickerHelper(getCombinedTicker(t1, t2));
        }

        @Override
        public <C extends BlockEntityController> BlockEntityTicker<C> createAsyncBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
            BlockEntityTicker<BlockEntityController> t1 = this.first.createAsyncBlockEntityTicker(world, blockState);
            BlockEntityTicker<BlockEntityController> t2 = this.second.createAsyncBlockEntityTicker(world, blockState);
            return createTickerHelper(getCombinedTicker(t1, t2));
        }

        private BlockEntityTicker<BiController> getCombinedTicker(BlockEntityTicker<BlockEntityController> t1, BlockEntityTicker<BlockEntityController> t2) {
            if (t1 == null && t2 == null) return null;
            if (t1 == null) return (w, p, s, bi) -> t2.tick(w, p, s, bi.second);
            if (t2 == null) return (w, p, s, bi) -> t1.tick(w, p, s, bi.first);
            return (w, p, s, bi) -> {
                t1.tick(w, p, s, bi.first);
                t2.tick(w, p, s, bi.second);
            };
        }

        @Override
        public void onBlockStateChange(ImmutableBlockState state) {
            this.first.onBlockStateChange(state);
            this.second.onBlockStateChange(state);
        }

        @Override
        public void onRemove() {
            this.first.onRemove();
            this.second.onRemove();
        }

        @Override
        public void saveCustomData(CompoundTag data) {
            this.first.saveCustomData(data);
            this.second.saveCustomData(data);
        }

        @Override
        public void loadCustomData(CompoundTag data) {
            this.first.loadCustomData(data);
            this.second.loadCustomData(data);
        }

        @Override
        public <C extends BlockEntityController> void let(Class<C> clazz, int index, Consumer<C> consumer) {
            if (index == 0) this.first.let(clazz, index, consumer);
            else if (index == 1) this.second.let(clazz, index, consumer);
        }

        @Override
        public <C extends BlockEntityController, V> V let(Class<C> clazz, int index, Function<C, V> function) {
            if (index == 0) return this.first.let(clazz, index, function);
            else if (index == 1) return this.second.let(clazz, index, function);
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C extends BlockEntityController> C get(Class<C> clazz, int index) {
            if (index == 0) return clazz.isInstance(this.first) ? (C) this.first : null;
            if (index == 1) return clazz.isInstance(this.second) ? (C) this.second : null;
            return null;
        }
    }

    private static final class CompositeController extends BlockEntityController {
        private final BlockEntityController[] controllers;

        public CompositeController(BlockEntity blockEntity, BlockEntityController[] controllers) {
            super(blockEntity);
            this.controllers = controllers;
        }

        @Override
        public boolean hasElement() {
            for (BlockEntityController controller : this.controllers) {
                if (controller.hasElement()) return true;
            }
            return false;
        }

        @Override
        public void gatherElements(Consumer<BlockEntityElement> consumer) {
            for (BlockEntityController controller : this.controllers) {
                controller.gatherElements(consumer);
            }
        }

        @Override
        public <C extends BlockEntityController> BlockEntityTicker<C> createBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
            return createCombinedTicker(world, blockState, BlockEntityController::createBlockEntityTicker);
        }

        @Override
        public <C extends BlockEntityController> BlockEntityTicker<C> createAsyncBlockEntityTicker(CEWorld world, ImmutableBlockState blockState) {
            return createCombinedTicker(world, blockState, BlockEntityController::createAsyncBlockEntityTicker);
        }

        @SuppressWarnings("unchecked")
        private <C extends BlockEntityController> BlockEntityTicker<C> createCombinedTicker(
                CEWorld world,
                ImmutableBlockState blockState,
                TickerExtractor extractor) {

            List<BlockEntityTicker<BlockEntityController>> tickers = new ArrayList<>(4);
            List<BlockEntityController> activeControllers = new ArrayList<>(4);

            for (BlockEntityController controller : this.controllers) {
                BlockEntityTicker<BlockEntityController> ticker = extractor.extract(controller, world, blockState);
                if (ticker != null) {
                    tickers.add(ticker);
                    activeControllers.add(controller);
                }
            }

            if (tickers.isEmpty()) return null;

            if (tickers.size() == 1) {
                BlockEntityTicker<BlockEntityController> firstTicker = tickers.getFirst();
                BlockEntityController firstController = activeControllers.getFirst();
                return (ceWorld, pos, state, controller) -> firstTicker.tick(ceWorld, pos, state, firstController);
            }

            BlockEntityTicker<BlockEntityController>[] tickersArray = tickers.toArray(new BlockEntityTicker[0]);
            BlockEntityController[] controllersArray = activeControllers.toArray(new BlockEntityController[0]);

            return (ceWorld, pos, state, controller) -> {
                for (int i = 0; i < tickersArray.length; i++) {
                    tickersArray[i].tick(ceWorld, pos, state, controllersArray[i]);
                }
            };
        }

        @FunctionalInterface
        private interface TickerExtractor {
            BlockEntityTicker<BlockEntityController> extract(BlockEntityController controller, CEWorld world, ImmutableBlockState blockState);
        }

        @Override
        public void onBlockStateChange(ImmutableBlockState state) {
            for (BlockEntityController controller : this.controllers) {
                controller.onBlockStateChange(state);
            }
        }

        @Override
        public <C extends BlockEntityController> void let(Class<C> clazz, int index, Consumer<C> consumer) {
            if (index >= 0 && index < this.controllers.length) {
                this.controllers[index].let(clazz, index, consumer);
            }
        }

        @Override
        public <C extends BlockEntityController, V> V let(Class<C> clazz, int index, Function<C, V> function) {
            if (index >= 0 && index < this.controllers.length) {
                return this.controllers[index].let(clazz, index, function);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C extends BlockEntityController> C get(Class<C> clazz, int index) {
            if (index >= 0 && index < this.controllers.length) {
                return (C) this.controllers[index];
            }
            return null;
        }

        @Override
        public void saveCustomData(CompoundTag data) {
            for (BlockEntityController controller : this.controllers) {
                controller.saveCustomData(data);
            }
        }

        @Override
        public void loadCustomData(CompoundTag data) {
            for (BlockEntityController controller : this.controllers) {
                controller.loadCustomData(data);
            }
        }
    }
}
