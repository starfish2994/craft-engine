package net.momirealms.craftengine.core.attribute.equipment;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class EquipmentSlotGroup {
    private static final Map<String, EquipmentSlotGroup> BY_NAME = new HashMap<>();
    public static final EquipmentSlotGroup ANY = register("any", slot -> true);
    public static final EquipmentSlotGroup MAINHAND = register("mainhand", slot -> slot == EquipmentSetSlot.MAINHAND);
    public static final EquipmentSlotGroup OFFHAND = register("offhand", slot -> slot == EquipmentSetSlot.OFFHAND);
    public static final EquipmentSlotGroup HAND = register("hand", slot -> slot == EquipmentSetSlot.MAINHAND || slot == EquipmentSetSlot.OFFHAND);
    public static final EquipmentSlotGroup FEET = register("feet", slot -> slot == EquipmentSetSlot.FEET);
    public static final EquipmentSlotGroup LEGS = register("legs", slot -> slot == EquipmentSetSlot.LEGS);
    public static final EquipmentSlotGroup CHEST = register("chest", slot -> slot == EquipmentSetSlot.CHEST);
    public static final EquipmentSlotGroup HEAD = register("head", slot -> slot == EquipmentSetSlot.HEAD);
    public static final EquipmentSlotGroup ARMOR = register("armor", slot -> slot == EquipmentSetSlot.HEAD || slot == EquipmentSetSlot.CHEST || slot == EquipmentSetSlot.LEGS || slot == EquipmentSetSlot.FEET);
    public static final EquipmentSlotGroup BODY = register("body", slot -> slot == EquipmentSetSlot.BODY);
    public static final EquipmentSlotGroup SADDLE = register("saddle", slot -> slot == EquipmentSetSlot.SADDLE);
    private final String name;
    private final Predicate<EquipmentSetSlot> predicate;

    private EquipmentSlotGroup(String name, Predicate<EquipmentSetSlot> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public String name() {
        return this.name;
    }

    public boolean test(EquipmentSetSlot slot) {
        return this.predicate.test(slot);
    }

    public static EquipmentSlotGroup register(String name, Predicate<EquipmentSetSlot> predicate) {
        EquipmentSlotGroup group = new EquipmentSlotGroup(name, predicate);
        if (BY_NAME.putIfAbsent(name, group) != null) throw new IllegalArgumentException("Slot group with name " + name + " already exists");
        return group;
    }

    @Nullable
    public static EquipmentSlotGroup byName(String name) {
        return BY_NAME.get(name);
    }

    @Nullable
    public static EquipmentSlotGroup byNameOrSlot(String name) {
        EquipmentSlotGroup group = BY_NAME.get(name);
        if (group != null) return group;
        EquipmentSetSlot slot = EquipmentSetSlot.byName(name);
        if (slot == null) return null;
        EquipmentSlotGroup single = new EquipmentSlotGroup(name, s -> s == slot);
        EquipmentSlotGroup previous = BY_NAME.putIfAbsent(name, single);
        return previous != null ? previous : single;
    }
}
