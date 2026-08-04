package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CEReloadedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // ce reloaded
    //
    // @Group Core
    //
    // @Triggers when the CraftEngine plugin finishes reloading.
    // The word "ce" in the event line can also be written as "craftengine".
    //
    // @Example
    // # Announce a message when CraftEngine finishes reloading.
    // on ce reloaded:
    // - announce "CraftEngine has been reloaded!"
    // -->

    public CEReloadedScriptEvent() {
        registerCouldMatcher("ce reloaded");
        registerCouldMatcher("craftengine reloaded");
    }

    public CraftEngineReloadEvent event;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @EventHandler
    public void onCEReloaded(CraftEngineReloadEvent event) {
        this.event = event;
        fire();
    }
}
