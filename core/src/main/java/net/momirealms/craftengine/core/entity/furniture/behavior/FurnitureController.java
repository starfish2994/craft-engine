package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.tick.FurnitureTicker;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class FurnitureController {
    protected final Furniture furniture;

    protected FurnitureController(@NotNull Furniture furniture) {
        this.furniture = furniture;
    }

    public Furniture furniture() {
        return this.furniture;
    }

    @SuppressWarnings("unchecked")
    public static <C extends FurnitureController, T extends FurnitureController> FurnitureTicker<C> createTickerHelper(FurnitureTicker<? super T> ticker) {
        return (FurnitureTicker<C>) ticker;
    }

    /**
     * Creates a ticker that runs on the main server thread.
     */
    public <T extends FurnitureController> FurnitureTicker<T> createFurnitureTicker() {
        return null;
    }

    /**
     * Creates a ticker that runs asynchronously.
     */
    public <T extends FurnitureController> FurnitureTicker<T> createAsyncFurnitureTicker() {
        return null;
    }

    public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
        return InteractionResult.TRY_EMPTY_HAND;
    }

    public InteractionResult useWithoutItem(InteractEntityContext context) {
        return InteractionResult.PASS;
    }

    public void onVariantChange() {
    }

    public void createFurnitureElements(Consumer<FurnitureElement> consumer) {
    }

    public void createFurnitureHitboxes(Consumer<FurnitureHitBox> consumer) {
    }

    /**
     * Triggered when the furniture is broken.
     */
    public void onDestroy(@Nullable Player player) {
    }

    /**
     * Triggered when the furniture is first placed in the world.
     */
    public void onPlace(UseOnContext context) {
    }

    /**
     * Triggered when the chunk containing the furniture is unloaded.
     */
    public void onUnload() {
    }

    /**
     * Triggered when the furniture is loaded into the world during chunk load.
     */
    public void onLoad() {
    }

    @Nullable
    public Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
        return null;
    }

    public static FurnitureController createController(@NotNull Furniture furniture) {
        List<FurnitureBehaviorTemplate> behaviors = furniture.config.behaviors();
        return switch (behaviors.size()) {
            case 0 -> new EmptyFurnitureBehaviorTemplate.EmptyFurnitureController(furniture);
            case 1 -> behaviors.getFirst().createController(furniture);
            case 2 -> new BiController(furniture, behaviors.getFirst().createController(furniture), behaviors.getLast().createController(furniture));
            default -> {
                FurnitureController[] controllers = new FurnitureController[behaviors.size()];
                for (int i = 0; i < behaviors.size(); i++) {
                    controllers[i] = behaviors.get(i).createController(furniture);
                }
                yield new CompositeController(furniture, controllers);
            }
        };
    }

    private static final class BiController extends FurnitureController {
        private final FurnitureController first;
        private final FurnitureController second;

        private BiController(Furniture furniture, FurnitureController first, FurnitureController second) {
            super(furniture);
            this.first = first;
            this.second = second;
        }

        @Override
        public <C extends FurnitureController> FurnitureTicker<C> createFurnitureTicker() {
            FurnitureTicker<FurnitureController> firstFurnitureTicker = this.first.createFurnitureTicker();
            FurnitureTicker<FurnitureController> secondFurnitureTicker = this.second.createFurnitureTicker();
            return createTickerHelper(gettFurnitureTicker(firstFurnitureTicker, secondFurnitureTicker));
        }

        @Override
        public <C extends FurnitureController> FurnitureTicker<C> createAsyncFurnitureTicker() {
            FurnitureTicker<FurnitureController> firstFurnitureTicker = this.first.createAsyncFurnitureTicker();
            FurnitureTicker<FurnitureController> secondFurnitureTicker = this.second.createAsyncFurnitureTicker();
            return createTickerHelper(gettFurnitureTicker(firstFurnitureTicker, secondFurnitureTicker));
        }

        private static FurnitureTicker<BiController> gettFurnitureTicker(FurnitureTicker<FurnitureController> firstFurnitureTicker, FurnitureTicker<FurnitureController> secondFurnitureTicker) {
            if (firstFurnitureTicker == null && secondFurnitureTicker == null) {
                return null;
            }
            if (firstFurnitureTicker == null) {
                return biController -> secondFurnitureTicker.tick(biController.second);
            }
            if (secondFurnitureTicker == null) {
                return biController -> firstFurnitureTicker.tick(biController.first);
            }
            return biController -> {
                firstFurnitureTicker.tick(biController.first);
                secondFurnitureTicker.tick(biController.second);
            };
        }

        @Override
        public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
            InteractionResult result = this.first.useOnFurniture(hitBox, context);
            if (result != InteractionResult.PASS && result != InteractionResult.TRY_EMPTY_HAND) {
                return result;
            }
            return this.second.useOnFurniture(hitBox, context);
        }

        @Override
        public InteractionResult useWithoutItem(InteractEntityContext context) {
            InteractionResult result = this.first.useWithoutItem(context);
            return result == InteractionResult.PASS ? this.second.useWithoutItem(context) : result;
        }

        @Override
        public void createFurnitureElements(Consumer<FurnitureElement> consumer) {
            this.first.createFurnitureElements(consumer);
            this.second.createFurnitureElements(consumer);
        }

        @Override
        public void createFurnitureHitboxes(Consumer<FurnitureHitBox> consumer) {
            this.first.createFurnitureHitboxes(consumer);
            this.second.createFurnitureHitboxes(consumer);
        }

        @Override
        public void onDestroy(Player player) {
            this.first.onDestroy(player);
            this.second.onDestroy(player);
        }

        @Override
        public void onPlace(UseOnContext context) {
            this.first.onPlace(context);
            this.second.onPlace(context);
        }

        @Override
        public void onUnload() {
            this.first.onUnload();
            this.second.onUnload();
        }

        @Override
        public void onLoad() {
            this.first.onLoad();
            this.second.onLoad();
        }

        @Override
        public void onVariantChange() {
            this.first.onVariantChange();
            this.second.onVariantChange();
        }

        @Override
        public @Nullable Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
            Item firstItemToPickup = this.first.getItemToPickup(player, hitBox);
            return firstItemToPickup != null ? firstItemToPickup : this.second.getItemToPickup(player, hitBox);
        }
    }

    private static final class CompositeController extends FurnitureController {
        private final FurnitureController[] controllers;

        private CompositeController(Furniture furniture, FurnitureController[] controllers) {
            super(furniture);
            this.controllers = controllers;
        }

        @Override
        public <T extends FurnitureController> FurnitureTicker<T> createFurnitureTicker() {
            return createCombinedTicker(FurnitureController::createFurnitureTicker);
        }

        @Override
        public <T extends FurnitureController> FurnitureTicker<T> createAsyncFurnitureTicker() {
            return createCombinedTicker(FurnitureController::createAsyncFurnitureTicker);
        }

        @SuppressWarnings("unchecked")
        private <T extends FurnitureController> FurnitureTicker<T> createCombinedTicker(
                java.util.function.Function<FurnitureController, FurnitureTicker<FurnitureController>> tickerExtractor) {

            List<FurnitureTicker<FurnitureController>> furnitureTickers = new ArrayList<>();
            List<FurnitureController> controllers = new ArrayList<>();

            for (FurnitureController controller : this.controllers) {
                FurnitureTicker<FurnitureController> ticker = tickerExtractor.apply(controller);
                if (ticker != null) {
                    furnitureTickers.add(ticker);
                    controllers.add(controller);
                }
            }

            if (furnitureTickers.isEmpty()) return null;

            if (furnitureTickers.size() == 1) {
                return createTickerHelper(tickSingle(controllers.getFirst(), furnitureTickers.getFirst()));
            }

            FurnitureTicker<FurnitureController>[] tickersArray = furnitureTickers.toArray(new FurnitureTicker[0]);
            FurnitureController[] controllersArray = controllers.toArray(new FurnitureController[0]);

            return controller -> {
                for (int i = 0; i < controllersArray.length; i++) {
                    tickersArray[i].tick(controllersArray[i]);
                }
            };
        }

        private static FurnitureTicker<CompositeController> tickSingle(FurnitureController controller, FurnitureTicker<FurnitureController> ticker) {
            return (c) -> {
                ticker.tick(controller);
            };
        }

        @Override
        public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
            for (FurnitureController controller : this.controllers) {
                InteractionResult result = controller.useOnFurniture(hitBox, context);
                if (result != InteractionResult.PASS && result != InteractionResult.TRY_EMPTY_HAND) {
                    return result;
                }
            }
            return InteractionResult.TRY_EMPTY_HAND;
        }

        @Override
        public InteractionResult useWithoutItem(InteractEntityContext context) {
            for (FurnitureController controller : this.controllers) {
                InteractionResult result = controller.useWithoutItem(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        }

        @Override
        public void createFurnitureElements(Consumer<FurnitureElement> consumer) {
            for (FurnitureController controller : this.controllers) {
                controller.createFurnitureElements(consumer);
            }
        }

        @Override
        public void createFurnitureHitboxes(Consumer<FurnitureHitBox> consumer) {
            for (FurnitureController controller : this.controllers) {
                controller.createFurnitureHitboxes(consumer);
            }
        }

        @Override
        public void onDestroy(Player player) {
            for (FurnitureController controller : this.controllers) {
                controller.onDestroy(player);
            }
        }

        @Override
        public void onPlace(UseOnContext context) {
            for (FurnitureController controller : this.controllers) {
                controller.onPlace(context);
            }
        }

        @Override
        public void onUnload() {
            for (FurnitureController controller : this.controllers) {
                controller.onUnload();
            }
        }

        @Override
        public void onLoad() {
            for (FurnitureController controller : this.controllers) {
                controller.onLoad();
            }
        }

        @Override
        public void onVariantChange() {
            for (FurnitureController controller : this.controllers) {
                controller.onVariantChange();
            }
        }

        @Override
        public @Nullable Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
            for (FurnitureController controller : this.controllers) {
                Item itemToPickup = controller.getItemToPickup(player, hitBox);
                if (itemToPickup != null) {
                    return itemToPickup;
                }
            }
            return null;
        }
    }
}
