package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import com.mojang.datafixers.util.Either;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;

public class SmithingTrimDemoSlotDisplay<I> implements SlotDisplay<I> {
    private final SlotDisplay<I> base;
    private final SlotDisplay<I> material;
    // 1.21.2-1.21.4
    private SlotDisplay<I> trimPattern;
    // 1.21.5
    private Either<Integer, TrimPattern> either;

    public SmithingTrimDemoSlotDisplay(SlotDisplay<I> base, SlotDisplay<I> material, SlotDisplay<I> trimPattern) {
        this.base = base;
        this.material = material;
        this.trimPattern = trimPattern;
    }

    public SmithingTrimDemoSlotDisplay(SlotDisplay<I> base, SlotDisplay<I> material, Either<Integer, TrimPattern> either) {
        this.base = base;
        this.either = either;
        this.material = material;
    }

    public static <I> SmithingTrimDemoSlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        SlotDisplay<I> base = SlotDisplay.read(buf, reader);
        SlotDisplay<I> material = SlotDisplay.read(buf, reader);
        if (VersionHelper.isOrAbove1_21_5()) {
            Either<Integer, TrimPattern> either = buf.readHolder(byteBuf -> {
                Key assetId = buf.readKey();
                Component component = AdventureHelper.nbtToComponent(buf.readNbt(false));
                boolean decal = buf.readBoolean();
                return new TrimPattern(assetId, component, decal);
            });
            return new SmithingTrimDemoSlotDisplay<>(base, material, either);
        } else {
            SlotDisplay<I> trimPattern = SlotDisplay.read(buf, reader);
            return new SmithingTrimDemoSlotDisplay<>(base, material, trimPattern);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(5);
        this.base.write(buf, writer);
        this.material.write(buf, writer);
        if (VersionHelper.isOrAbove1_21_5()) {
            buf.writeHolder(this.either, (byteBuf, pattern) -> {
                byteBuf.writeKey(pattern.assetId);
                byteBuf.writeNbt(AdventureHelper.componentToNbt(pattern.description), false);
                byteBuf.writeBoolean(pattern.decal);
            });
        } else {
            this.trimPattern.write(buf, writer);
        }
    }

    @Override
    public String toString() {
        return "SmithingTrimDemoSlotDisplay{" +
                "base=" + base +
                ", material=" + material +
                ", trimPattern=" + trimPattern +
                ", either=" + either +
                '}';
    }

    public record TrimPattern(Key assetId, Component description, boolean decal) {

        @Override
        public @NotNull String toString() {
            return "TrimPattern{" +
                    "assetId=" + assetId +
                    ", description=" + description +
                    ", decal=" + decal +
                    '}';
        }
    }
}
