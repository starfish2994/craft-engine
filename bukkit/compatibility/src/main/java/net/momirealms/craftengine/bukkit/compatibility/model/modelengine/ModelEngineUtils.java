package net.momirealms.craftengine.bukkit.compatibility.model.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import net.momirealms.craftengine.bukkit.block.entity.renderer.element.BukkitBlockEntityElementConfigs;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Entity;

public class ModelEngineUtils {

    public static void bindModel(Entity base, String id) {
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(base);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(id);
        modeledEntity.addModel(activeModel, true);
    }

    public static int interactionToBaseEntity(int entityId) {
        ActiveModel activeModel = ModelEngineAPI.getInteractionTracker().getModelRelay(entityId);
        if (activeModel != null) {
            ModeledEntity modeledEntity = activeModel.getModeledEntity();
            if (modeledEntity == null) {
                return entityId;
            }
            return modeledEntity.getBase().getEntityId();
        }
        return entityId;
    }

    public static void registerConstantBlockEntityRender() {
        BukkitBlockEntityElementConfigs.register(Key.of("craftengine:model_engine"), new ModelEngineBlockEntityElementConfig.Factory());
    }
}
