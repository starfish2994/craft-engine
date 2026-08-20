package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptPlaceEvent;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerAttemptsPlaceCEFurnitureScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player attempts to place ce furniture <'furniture'>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event when the player is placing the furniture with a specified item.
    //
    // @Triggers when a player is about to place a CraftEngine furniture, before the placement happens.
    // Cancelling the event prevents the placement.
    // The word "ce" in the event line can also be written as "craftengine".
    //
    // @Context
    // <context.location> returns the LocationTag the furniture would be placed at.
    // <context.id> returns the CraftEngine furniture id of the furniture being placed, like "default:wooden_chair".
    // <context.variant> returns the name of the furniture variant being placed.
    // <context.clicked_block> returns the LocationTag of the block that was clicked to trigger the placement.
    // <context.hand> returns an ElementTag of the used hand.
    //
    // @Player Always.
    //
    // @Example
    // # Prevent placing any CraftEngine furniture in a region.
    // on player attempts to place ce furniture furniture in:no_build_zone:
    // - narrate "You can't place that here!"
    // - determine cancelled
    // -->

    public PlayerAttemptsPlaceCEFurnitureScriptEvent() {
        registerCouldMatcher("player attempts to place ce furniture <'furniture'>");
        registerCouldMatcher("player attempts to place craftengine furniture <'furniture'>");
        registerSwitches("with");
    }

    public LocationTag location;
    public String id;
    public FurnitureAttemptPlaceEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String matcher = path.eventArgLowerAt(6);
        if (!matcher.equals("furniture") && !createMatcher(matcher).doesMatch(this.id)) {
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
            case "variant" -> new ElementTag(this.event.variant().name());
            case "clicked_block" -> new LocationTag(this.event.clickedBlock().getLocation());
            case "hand" -> new ElementTag(this.event.hand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerAttemptsPlaceCEFurniture(FurnitureAttemptPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.location = new LocationTag(event.location());
        this.id = event.furniture().id().toString();
        this.event = event;
        fire(event);
    }
}
