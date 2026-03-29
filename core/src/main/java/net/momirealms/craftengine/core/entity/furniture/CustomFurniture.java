package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
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
    List<FurnitureBehavior> behaviors();

    default FurnitureBehavior.Handler createHandler(Furniture furniture) {
        List<FurnitureBehavior> behaviors = this.behaviors();
        return switch (behaviors.size()) {
            case 0 -> new FurnitureBehavior.Handler(furniture) {};
            case 1 -> behaviors.getFirst().createHandler(furniture);
            case 2 -> new FurnitureBehavior.BiHandler(furniture, behaviors.get(0).createHandler(furniture), behaviors.get(1).createHandler(furniture));
            default -> {
                FurnitureBehavior.Handler[] handlers = new FurnitureBehavior.Handler[behaviors.size()];
                for (int i = 0; i < behaviors.size(); i++) {
                    handlers[i] = behaviors.get(i).createHandler(furniture);
                }
                yield new FurnitureBehavior.CompositeHandler(furniture, handlers);
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
