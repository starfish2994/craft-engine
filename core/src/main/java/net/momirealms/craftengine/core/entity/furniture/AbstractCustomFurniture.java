package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCustomFurniture implements CustomFurniture {
    private final Key id;
    private final FurnitureSettings settings;
    private final Map<AnchorType, Placement> placements;
    private final Map<EventTrigger, List<Function<Context>>> events;
    @Nullable
    private final LootTable<?> lootTable;

    private final AnchorType anyType;

    protected AbstractCustomFurniture(@NotNull Key id,
                           @NotNull FurnitureSettings settings,
                           @NotNull Map<AnchorType, Placement> placements,
                           @NotNull Map<EventTrigger, List<Function<Context>>> events,
                           @Nullable LootTable<?> lootTable) {
        this.id = id;
        this.settings = settings;
        this.placements = placements;
        this.lootTable = lootTable;
        this.events = events;
        this.anyType = placements.keySet().stream().findFirst().orElse(null);
    }

    @Override
    public void execute(Context context, EventTrigger trigger) {
        for (Function<Context> function : Optional.ofNullable(this.events.get(trigger)).orElse(Collections.emptyList())) {
            function.run(context);
        }
    }

    @Override
    public Key id() {
        return this.id;
    }

    @Override
    public Map<AnchorType, Placement> placements() {
        return this.placements;
    }

    @Override
    public FurnitureSettings settings() {
        return this.settings;
    }

    @Override
    public @Nullable LootTable<?> lootTable() {
        return this.lootTable;
    }

    @Override
    public AnchorType getAnyAnchorType() {
        return this.anyType;
    }

    @Override
    public boolean isAllowedPlacement(AnchorType anchorType) {
        return this.placements.containsKey(anchorType);
    }

    @Override
    public Placement getPlacement(AnchorType anchorType) {
        return this.placements.get(anchorType);
    }

    @Override
    public Placement getValidPlacement(AnchorType anchorType) {
        Placement placement = this.placements.get(anchorType);
        if (placement == null) {
            return this.placements.get(getAnyAnchorType());
        }
        return placement;
    }
}
