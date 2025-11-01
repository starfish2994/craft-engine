package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Furniture {
    void initializeColliders();

    WorldPosition position();

    boolean isValid();

    void destroy();

    void destroyColliders();

    void destroySeats();

    UUID uuid();

    int baseEntityId();

    @Nullable
    HitBox hitBoxByEntityId(int id);

    @Nullable HitBoxPart hitBoxPartByEntityId(int id);

    @NotNull
    AnchorType anchorType();

    @NotNull
    Key id();

    @NotNull
    CustomFurniture config();

    boolean hasExternalModel();

    FurnitureExtraData extraData();

    void setExtraData(FurnitureExtraData extraData);

    void save();
}
