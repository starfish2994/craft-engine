package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.ArrayList;
import java.util.List;

public final class ModernItemModel {
    private final ItemModel itemModel;
    private final boolean oversizedInGui;
    private final boolean handAnimationOnSwap;
    private final float swapAnimationScale;
    private final Transformation transformation;

    public ModernItemModel(ItemModel itemModel, boolean handAnimationOnSwap, boolean oversizedInGui, float swapAnimationScale, Transformation transformation) {
        this.handAnimationOnSwap = handAnimationOnSwap;
        this.itemModel = itemModel;
        this.oversizedInGui = oversizedInGui;
        this.swapAnimationScale = swapAnimationScale;
        this.transformation = transformation;

    }

    public static ModernItemModel fromJson(JsonObject json) {
        ItemModel model = ItemModels.fromJson(json.getAsJsonObject("model"));
        return new ModernItemModel(
                model,
                GsonHelper.getAsBoolean(json.get("hand_animation_on_swap"), true),
                GsonHelper.getAsBoolean(json.get("oversized_in_gui"), false),
                GsonHelper.getAsFloat(json.get("swap_animation_scale"), 1.0f),
                json.has("transformation") ? Transformation.fromJson(json.get("transformation")) : null
        );
    }

    public JsonObject toJson(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        if (this.oversizedInGui && version.isAtOrAbove(MinecraftVersion.V1_21_6)) {
            json.addProperty("oversized_in_gui", true);
        }
        if (!this.handAnimationOnSwap) {
            json.addProperty("hand_animation_on_swap", false);
        }
        if (this.swapAnimationScale != 1.0f && version.isAtOrAbove(MinecraftVersion.V1_21_11)) {
            json.addProperty("swap_animation_scale", this.swapAnimationScale);
        }
        json.add("model", this.itemModel.apply(version));
        if (version.isAtOrAbove(MinecraftVersion.V26_1) && this.transformation != null) {
            json.add("transformation", this.transformation.toJson());
        }
        return json;
    }

    public List<Revision> revisions() {
        List<Revision> revisions = new ArrayList<>(4);
        this.itemModel.collectRevision(revisions::add);
        return revisions.stream().distinct().toList();
    }

    public boolean handAnimationOnSwap() {
        return this.handAnimationOnSwap;
    }

    public ItemModel itemModel() {
        return this.itemModel;
    }

    public boolean oversizedInGui() {
        return this.oversizedInGui;
    }

    public float swapAnimationScale() {
        return this.swapAnimationScale;
    }

    public Transformation transformation() {
        return this.transformation;
    }
}
