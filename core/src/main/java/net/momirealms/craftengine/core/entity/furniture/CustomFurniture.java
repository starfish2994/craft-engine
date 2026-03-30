package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.behavior.Controller;
import net.momirealms.craftengine.core.entity.furniture.behavior.EmptyFurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface CustomFurniture {

    void execute(Context context, EventTrigger trigger);

    Key id();

    FurnitureSettings settings();

    default String translationKey() {
        Key id = this.id();
        return "furniture." + id.namespace() + "." + id.value();
    }

    @Nullable
    LootTable lootTable();

    Map<String, FurnitureVariant> variants();

    default FurnitureVariant anyVariant() {
        return variants().values().iterator().next();
    }

    default String anyVariantName() {
        return variants().keySet().iterator().next();
    }

    @Nullable
    FurnitureVariant getVariant(String variantName);

    @NotNull
    List<FurnitureBehaviorTemplate> behaviors();

    default Controller createController(Furniture furniture) {
        List<FurnitureBehaviorTemplate> behaviors = this.behaviors();
        return switch (behaviors.size()) {
            case 0 -> new EmptyFurnitureBehaviorTemplate.EmptyFurnitureController(furniture);
            case 1 -> behaviors.getFirst().createController(furniture);
            case 2 -> new Controller.BiController(furniture, behaviors.get(0).createController(furniture), behaviors.get(1).createController(furniture));
            default -> {
                Controller[] controllers = new Controller[behaviors.size()];
                for (int i = 0; i < behaviors.size(); i++) {
                    controllers[i] = behaviors.get(i).createController(furniture);
                }
                yield new Controller.CompositeController(furniture, controllers);
            }
        };
    }

    @NotNull
    default FurnitureVariant getVariant(FurniturePersistentData accessor) {
        Optional<String> optionalVariant = accessor.variant();
        String variantName = null;
        if (optionalVariant.isPresent()) {
            variantName = optionalVariant.get();
        } else {
            Optional<AnchorType> optionalAnchorType = accessor.anchorType();
            if (optionalAnchorType.isPresent()) {
                variantName = optionalAnchorType.get().name().toLowerCase(Locale.ROOT);
                accessor.setVariant(variantName);
                accessor.removeCustomData(FurniturePersistentData.ANCHOR_TYPE);
            }
        }
        if (variantName == null) {
            return anyVariant();
        }
        FurnitureVariant variant = getVariant(variantName);
        if (variant == null) {
            return anyVariant();
        }
        return variant;

    }

    static Builder builder() {
        return new CustomFurnitureImpl.BuilderImpl();
    }

    interface Builder {

        Builder id(Key id);

        Builder variants(Map<String, FurnitureVariant> variants);

        Builder settings(FurnitureSettings settings);

        Builder lootTable(LootTable lootTable);

        Builder events(Map<EventTrigger, List<Function<Context>>> events);

        CustomFurniture build();
    }
}
