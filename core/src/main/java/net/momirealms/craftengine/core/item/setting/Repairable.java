package net.momirealms.craftengine.core.item.setting;

import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.Tristate;

import java.util.Map;

public record Repairable(Tristate craftingTable, Tristate anvilRepair, Tristate anvilCombine) {

    public static final Repairable UNDEFINED = new Repairable(Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED);
    public static final Repairable TRUE = new Repairable(Tristate.TRUE, Tristate.TRUE, Tristate.TRUE);
    public static final Repairable FALSE = new Repairable(Tristate.FALSE, Tristate.FALSE, Tristate.FALSE);

    public static Repairable fromMap(Map<String, Object> map) {
        Tristate craftingTable = map.containsKey("crafting-table") ? Tristate.of(ResourceConfigUtils.getAsBoolean(map.get("crafting-table"), "crafting-table")) : Tristate.UNDEFINED;
        Tristate anvilRepair = map.containsKey("anvil-repair") ? Tristate.of(ResourceConfigUtils.getAsBoolean(map.get("anvil-repair"), "anvil-repair")) : Tristate.UNDEFINED;
        Tristate anvilCombine = map.containsKey("anvil-combine") ? Tristate.of(ResourceConfigUtils.getAsBoolean(map.get("anvil-combine"), "anvil-combine")) : Tristate.UNDEFINED;
        return new Repairable(craftingTable, anvilRepair, anvilCombine);
    }
}
