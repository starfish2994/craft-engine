package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerPlacesCEBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places ce block <'block'>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event when the player is placing the block with a specified item.
    //
    // @Triggers when a player places a CraftEngine custom block.
    // The word "ce" in the event line can also be written as "craftengine".
    //
    // @Context
    // <context.location> returns the LocationTag the block was placed at.
    // <context.id> returns the CraftEngine block id of the block that was placed, like "default:togglable_light_block".
    // <context.hand> returns an ElementTag of the used hand.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate the CraftEngine block's id when a player places any CraftEngine block.
    // on player places ce block block:
    // - narrate "You just placed a <context.id>!"
    //
    // @Example
    // # Cancel placing a specific CraftEngine block in a region.
    // on player places ce block default:safe_block in:my_base:
    // - determine cancelled
    // -->

    public PlayerPlacesCEBlockScriptEvent() {
        registerCouldMatcher("player places ce block <'block'>");
        registerCouldMatcher("player places craftengine block <'block'>");
        registerSwitches("with");
    }

    public LocationTag location;
    public String id;
    public CustomBlockPlaceEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String matcher = path.eventArgLowerAt(4);
        if (!matcher.equals("block") && !createMatcher(matcher).doesMatch(this.id)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(this.event.getPlayer().getInventory().getItem(
                this.event.hand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)))) {
            return false;
        }
        if (!runInCheck(path, this.location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(this.event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> this.location;
            case "id" -> new ElementTag(this.id);
            case "hand" -> new ElementTag(this.event.hand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerPlacesCEBlock(CustomBlockPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.location = new LocationTag(event.location());
        this.id = event.customBlock().id().toString();
        this.event = event;
        fire(event);
    }
}
