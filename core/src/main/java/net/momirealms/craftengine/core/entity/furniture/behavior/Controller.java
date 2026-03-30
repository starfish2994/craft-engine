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
import java.util.function.Consumer;

public abstract class Controller {
    protected final Furniture furniture;

    public Controller(@NotNull Furniture furniture) {
        this.furniture = furniture;
    }

    /**
     * Creates a ticker that runs on the main server thread.
     */
    public <T extends Furniture> FurnitureTicker<T> createFurnitureTicker() {
        return null;
    }

    /**
     * Creates a ticker that runs asynchronously.
     */
    public <T extends Furniture> FurnitureTicker<T> createAsyncFurnitureTicker() {
        return null;
    }

    public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
        return InteractionResult.TRY_EMPTY_HAND;
    }

    public InteractionResult useWithoutItem(InteractEntityContext context) {
        return InteractionResult.PASS;
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
     * Triggered when the chunk containing the furniture is loaded into the world.
     */
    public void onLoad() {
    }

    @Nullable
    public Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
        return null;
    }

    public static class BiController extends Controller {
        protected final Controller first;
        protected final Controller second;

        public BiController(Furniture furniture, Controller first, Controller second) {
            super(furniture);
            this.first = first;
            this.second = second;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Furniture> FurnitureTicker<T> createFurnitureTicker() {
            FurnitureTicker<Furniture> firstFurnitureTicker = this.first.createFurnitureTicker();
            FurnitureTicker<Furniture> secondFurnitureTicker = this.second.createFurnitureTicker();
            if (firstFurnitureTicker == null && secondFurnitureTicker == null) {
                return null;
            }
            if (firstFurnitureTicker == null) {
                return (FurnitureTicker<T>) secondFurnitureTicker;
            }
            if (secondFurnitureTicker == null) {
                return (FurnitureTicker<T>) firstFurnitureTicker;
            }
            return furniture -> {
                firstFurnitureTicker.tick(furniture);
                secondFurnitureTicker.tick(furniture);
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Furniture> FurnitureTicker<T> createAsyncFurnitureTicker() {
            FurnitureTicker<Furniture> firstFurnitureTicker = this.first.createAsyncFurnitureTicker();
            FurnitureTicker<Furniture> secondFurnitureTicker = this.second.createAsyncFurnitureTicker();
            if (firstFurnitureTicker == null && secondFurnitureTicker == null) {
                return null;
            }
            if (firstFurnitureTicker == null) {
                return (FurnitureTicker<T>) secondFurnitureTicker;
            }
            if (secondFurnitureTicker == null) {
                return (FurnitureTicker<T>) firstFurnitureTicker;
            }
            return furniture -> {
                firstFurnitureTicker.tick(furniture);
                secondFurnitureTicker.tick(furniture);
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
        public @Nullable Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
            Item firstItemToPickup = this.first.getItemToPickup(player, hitBox);
            return firstItemToPickup != null ? firstItemToPickup : this.second.getItemToPickup(player, hitBox);
        }
    }

    // 复合家具行为处理器
    public static class CompositeController extends Controller {
        protected final Controller[] controllers;

        public CompositeController(Furniture furniture, Controller... controllers) {
            super(furniture);
            this.controllers = controllers;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Furniture> FurnitureTicker<T> createFurnitureTicker() {
            ArrayList<FurnitureTicker<T>> furnitureTickers = new ArrayList<>();
            for (int i = 0; i < this.controllers.length; i++) {
                FurnitureTicker<T> syncFurnitureTicker = this.controllers[i].createFurnitureTicker();
                if (syncFurnitureTicker != null) {
                    furnitureTickers.add(syncFurnitureTicker);
                }
            }
            if (furnitureTickers.isEmpty()) return null;
            if (furnitureTickers.size() == 1) return furnitureTickers.getFirst();
            // 新建一个包含所有tick任务的tick任务.
            FurnitureTicker<T>[] tickers = furnitureTickers.toArray(new FurnitureTicker[0]);
            return furniture -> {
                for (int i = 0; i < tickers.length; i++) {
                    tickers[i].tick(furniture);
                }
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Furniture> FurnitureTicker<T> createAsyncFurnitureTicker() {
            ArrayList<FurnitureTicker<T>> furnitureTickers = new ArrayList<>();
            for (int i = 0; i < this.controllers.length; i++) {
                FurnitureTicker<T> asyncFurnitureTicker = this.controllers[i].createAsyncFurnitureTicker();
                if (asyncFurnitureTicker != null) {
                    furnitureTickers.add(asyncFurnitureTicker);
                }
            }
            if (furnitureTickers.isEmpty()) return null;
            if (furnitureTickers.size() == 1) return furnitureTickers.getFirst();
            // 新建一个包含所有tick任务的tick任务.
            FurnitureTicker<T>[] tickers = furnitureTickers.toArray(new FurnitureTicker[0]);
            return furniture -> {
                for (int i = 0; i < tickers.length; i++) {
                    tickers[i].tick(furniture);
                }
            };
        }

        @Override
        public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
            for (int i = 0; i < this.controllers.length; i++) {
                InteractionResult result = this.controllers[i].useOnFurniture(hitBox, context);
                if (result != InteractionResult.PASS && result != InteractionResult.TRY_EMPTY_HAND) {
                    return result;
                }
            }
            return InteractionResult.TRY_EMPTY_HAND;
        }

        @Override
        public InteractionResult useWithoutItem(InteractEntityContext context) {
            for (int i = 0; i < this.controllers.length; i++) {
                InteractionResult result = this.controllers[i].useWithoutItem(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        }

        @Override
        public void createFurnitureElements(Consumer<FurnitureElement> consumer) {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].createFurnitureElements(consumer);
            }
        }

        @Override
        public void createFurnitureHitboxes(Consumer<FurnitureHitBox> consumer) {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].createFurnitureHitboxes(consumer);
            }
        }

        @Override
        public void onDestroy(Player player) {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].onDestroy(player);
            }
        }

        @Override
        public void onPlace(UseOnContext context) {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].onPlace(context);
            }
        }

        @Override
        public void onUnload() {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].onUnload();
            }
        }

        @Override
        public void onLoad() {
            for (int i = 0; i < this.controllers.length; i++) {
                this.controllers[i].onLoad();
            }
        }

        @Override
        public @Nullable Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
            for (int i = 0; i < this.controllers.length; i++) {
                Item itemToPickup = this.controllers[i].getItemToPickup(player, hitBox);
                if (itemToPickup != null) {
                    return itemToPickup;
                }
            }
            return null;
        }
    }
}
