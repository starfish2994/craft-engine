package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.DummyFunction;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EquipmentSet {
    private final Entry[] entries;

    public EquipmentSet(Entry[] entries) {
        this.entries = entries;
    }

    public static EquipmentSet fromConfig(ConfigSection section) {
        ConfigSection piecesSection = section.getNonNullSection("pieces");
        int maxPieces = 0;
        Map<Integer, Entry> entries = new HashMap<>();
        for (String pieceStr : piecesSection.keySet()) {
            int pieceCount = Integer.parseInt(pieceStr);
            maxPieces = Math.max(maxPieces, pieceCount);
            entries.put(pieceCount, Entry.fromConfig(piecesSection.getSection(pieceStr)));
        }
        Entry[] entriesArray = new Entry[maxPieces];
        for (Map.Entry<Integer, Entry> entry : entries.entrySet()) {
            entriesArray[entry.getKey() - 1] = entry.getValue();
        }
        return new EquipmentSet(entriesArray);
    }

    @Nullable
    public Entry entry(int tier) {
        if (tier < 1 || tier > this.entries.length) return null;
        return this.entries[tier - 1];
    }

    public int effectiveTier(int pieces) {
        if (pieces <= 0) return 0;
        int clamped = Math.min(pieces, this.entries.length);
        for (int i = clamped - 1; i >= 0; i--) {
            if (this.entries[i] != null) return i + 1;
        }
        return 0;
    }

    public List<AttributeModifierConfig> getAttributeModifiers(int pieces) {
        Entry entry = entry(effectiveTier(pieces));
        return entry == null ? List.of() : entry.modifiers;
    }

    public static class Entry {
        private static final String[] POTION_EFFECTS = ConfigKeys.of("potion_effect(s)");
        private final List<AttributeModifierConfig> modifiers;
        private final List<SetPotionEffect> potionEffects;
        private final Function<Context> onActivate;
        private final Function<Context> onDeactivate;

        public Entry(List<AttributeModifierConfig> modifiers,
                     List<SetPotionEffect> potionEffects,
                     Function<Context> onActivate,
                     Function<Context> onDeactivate) {
            this.modifiers = modifiers;
            this.potionEffects = potionEffects;
            this.onActivate = onActivate;
            this.onDeactivate = onDeactivate;
        }

        public static Entry fromConfig(ConfigSection section) {
            List<AttributeModifierConfig> modifiers = section.getSectionList("attribute", AttributeModifierConfig::fromConfig);
            List<SetPotionEffect> potionEffects = section.getSectionList(POTION_EFFECTS, SetPotionEffect::fromConfig);
            Function<Context> onActivate = DummyFunction.INSTANCE;
            Function<Context> onDeactivate = DummyFunction.INSTANCE;
            ConfigSection events = section.getSection("events");
            if (events != null) {
                onActivate = events.getValue("activate", CommonFunctions::fromConfig, DummyFunction.INSTANCE);
                onDeactivate = events.getValue("deactivate", CommonFunctions::fromConfig, DummyFunction.INSTANCE);
            }
            return new Entry(modifiers, potionEffects, onActivate, onDeactivate);
        }

        public List<AttributeModifierConfig> modifiers() {
            return this.modifiers;
        }

        public List<SetPotionEffect> potionEffects() {
            return this.potionEffects;
        }

        public Function<Context> onActivate() {
            return this.onActivate;
        }

        public Function<Context> onDeactivate() {
            return this.onDeactivate;
        }
    }
}
