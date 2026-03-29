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

import java.util.function.Consumer;

// 独立家具行为
@ApiStatus.Experimental
public abstract class FurnitureBehavior {
    public final CustomFurniture furniture;
    protected FurnitureBehavior(CustomFurniture furniture) {
        this.furniture = furniture;
    }

    public CustomFurniture furniture() {
        return this.furniture;
    }

    public abstract Handler createHandler(Furniture furniture);

    // 独立家具行为处理器.
    public static abstract class Handler {
        protected final Furniture furniture;

        public Handler(Furniture furniture) {
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
        public void onDestroy() {
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
    }
}
