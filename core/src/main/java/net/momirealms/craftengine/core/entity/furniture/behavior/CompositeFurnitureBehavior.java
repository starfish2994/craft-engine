package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.tick.FurnitureTicker;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// 复合家具行为
@ApiStatus.Experimental
public class CompositeFurnitureBehavior extends FurnitureBehavior {
    public final FurnitureBehavior[] furnitureBehaviors;

    public CompositeFurnitureBehavior(CustomFurniture furniture, List<FurnitureBehavior> furnitureBehaviors) {
        super(furniture);
        this.furnitureBehaviors = furnitureBehaviors.toArray(new FurnitureBehavior[0]);
    }

    @Override
    public Handler createHandler(Furniture furniture) {
        // 双行为处理器
        if (furnitureBehaviors.length == 2) {
            return new BiHandler(
                    furniture,
                    furnitureBehaviors[0].createHandler(furniture),
                    furnitureBehaviors[1].createHandler(furniture)
            );
        }
        // 复合行为处理器
        else {
            Handler[] handlers = new Handler[furnitureBehaviors.length];
            for (int i = 0; i < furnitureBehaviors.length; i++) {
                handlers[i] = furnitureBehaviors[i].createHandler(furniture);
            }
            return new CompositeHandler(furniture, handlers);
        }
    }

    // 双家具行为处理器
    public static class BiHandler extends Handler {
        protected final Handler first;
        protected final Handler second;

        public BiHandler(Furniture furniture, Handler first, Handler second) {
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
            return result == InteractionResult.PASS ? this.second.useOnFurniture(hitBox, context) : result;
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
        public void onDestroy() {
            this.first.onDestroy();
            this.second.onDestroy();
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
    public static class CompositeHandler extends Handler {
        protected final Handler[] handlers;

        public CompositeHandler(Furniture furniture, Handler... handlers) {
            super(furniture);
            this.handlers = handlers;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Furniture> FurnitureTicker<T> createFurnitureTicker() {
            ArrayList<FurnitureTicker<T>> furnitureTickers = new ArrayList<>();
            for (int i = 0; i < handlers.length; i++) {
                FurnitureTicker<T> syncFurnitureTicker = handlers[i].createFurnitureTicker();
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
            for (int i = 0; i < handlers.length; i++) {
                FurnitureTicker<T> asyncFurnitureTicker = handlers[i].createAsyncFurnitureTicker();
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
            InteractionResult result = InteractionResult.TRY_EMPTY_HAND;
            for (int i = 0; i < handlers.length; i++) {
                result = handlers[i].useOnFurniture(hitBox, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return result;
        }

        @Override
        public InteractionResult useWithoutItem(InteractEntityContext context) {
            InteractionResult result = InteractionResult.PASS;
            for (int i = 0; i < handlers.length; i++) {
                result = handlers[i].useWithoutItem(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return result;
        }

        @Override
        public void createFurnitureElements(Consumer<FurnitureElement> consumer) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].createFurnitureElements(consumer);
            }
        }

        @Override
        public void createFurnitureHitboxes(Consumer<FurnitureHitBox> consumer) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].createFurnitureHitboxes(consumer);
            }
        }

        @Override
        public void onDestroy() {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onDestroy();
            }
        }

        @Override
        public void onPlace(UseOnContext context) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onPlace(context);
            }
        }

        @Override
        public void onUnload() {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onUnload();
            }
        }

        @Override
        public void onLoad() {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onLoad();
            }
        }

        @Override
        public @Nullable Item getItemToPickup(Player player, FurnitureHitBox hitBox) {
            for (int i = 0; i < handlers.length; i++) {
                Item itemToPickup = handlers[i].getItemToPickup(player, hitBox);
                if (itemToPickup != null) {
                    return itemToPickup;
                }
            }
            return null;
        }
    }
}
