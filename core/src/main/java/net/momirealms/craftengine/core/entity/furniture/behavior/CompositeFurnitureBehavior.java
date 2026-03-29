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
        Handler[] handlers = new Handler[furnitureBehaviors.length];
        for (int i = 0; i < furnitureBehaviors.length; i++) {
            handlers[i] = furnitureBehaviors[i].createHandler(furniture);
        }
        return new CompositeHandler(furniture, handlers);
    }

    // 复合家具行为处理器
    public static class CompositeHandler extends Handler {
        protected final Handler[] handlers;

        public CompositeHandler(Furniture furniture, Handler... handlers) {
            super(furniture);
            this.handlers = handlers;
        }

        @Override
        public void onPlace(UseOnContext context) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onPlace(context);
            }
        }

        @Override
        public void onDestroy() {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].onDestroy();
            }
        }

        @Override
        public void createFurnitureHitboxes(Consumer<FurnitureHitBox> register) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].createFurnitureHitboxes(register);
            }
        }

        @Override
        public void createFurnitureElements(Consumer<FurnitureElement> register) {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].createFurnitureElements(register);
            }
        }

        @Override
        public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
            InteractionResult result = InteractionResult.TRY_EMPTY_HAND;
            for (int i = 0; i < handlers.length; i++) {
                result = handlers[i].useOnFurniture(hitBox, context);
                if (result.success()) {
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
                if (result.success()) {
                    return result;
                }
            }
            return result;
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
            FurnitureTicker<T>[] tickers = furnitureTickers.toArray(new FurnitureTicker[0]);
            // 新建一个包含所有tick任务的tick任务.
            return furniture -> {
                for (int i = 0; i < tickers.length; i++) {
                    tickers[i].tick(furniture);
                }
            };
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
            FurnitureTicker<T>[] tickers = furnitureTickers.toArray(new FurnitureTicker[0]);
            // 新建一个包含所有tick任务的tick任务.
            return furniture -> {
                for (int i = 0; i < tickers.length; i++) {
                    tickers[i].tick(furniture);
                }
            };
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
        public @Nullable Item getItemToPickup(Player player) {
            Item itemToPickup = null;
            for (int i = 0; i < handlers.length; i++) {
                itemToPickup = handlers[i].getItemToPickup(player);
            }
            return itemToPickup;
        }
    }
}
