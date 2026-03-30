package net.momirealms.craftengine.bukkit.compatibility.model.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import net.momirealms.craftengine.bukkit.entity.furniture.element.AbstractFurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class ModelEngineFurnitureElement extends AbstractFurnitureElement {
    public final Furniture furniture;
    public final ModelEngineFurnitureElementConfig config;
    private Dummy<?> dummy;

    ModelEngineFurnitureElement(Furniture furniture, ModelEngineFurnitureElementConfig config) {
        super(config.predicate, config.hasCondition);
        this.furniture = furniture;
        this.config = config;
        this.dummy = createDummy();
    }

    private Dummy<?> createDummy() {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(config.model);
        if (activeModel == null) {
            return null;
        } else {
            Dummy<?> dummy = new Dummy<>();
            WorldPosition pos = this.furniture.position();
            Vector3f offset = config.position;
            Location location = new Location((World) pos.world.platformWorld(), pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, config.yaw, config.pitch);
            dummy.syncLocation(location);
            dummy.setDetectingPlayers(false);
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
            modeledEntity.addModel(activeModel, false);
            return dummy;
        }
    }

    @Override
    public @NonNull Furniture furniture() {
        return this.furniture;
    }

    @Override
    public void showInternal(Player player) {
        if (this.dummy != null) {
            this.dummy.setForceViewing((org.bukkit.entity.Player) player.platformPlayer(), true);
        }
    }

    @Override
    public void hide(Player player) {
        if (this.dummy != null) {
            this.dummy.setForceHidden((org.bukkit.entity.Player) player.platformPlayer(), true);
        }
    }

    @Override
    public void refresh(Player player) {
        this.hide(player);
        this.show(player);
    }

    @Override
    public void deactivate() {
        if (this.dummy != null) {
            this.dummy.setRemoved(true);
            this.dummy = null;
        }
    }

    @Override
    public void activate() {
        if (this.dummy == null) {
            this.dummy = createDummy();
        }
    }

    @Override
    public void collectInteractableEntityId(Consumer<Integer> collector) {
        if (this.dummy != null) {
            collector.accept(this.dummy.getEntityId());
        }
    }
}
