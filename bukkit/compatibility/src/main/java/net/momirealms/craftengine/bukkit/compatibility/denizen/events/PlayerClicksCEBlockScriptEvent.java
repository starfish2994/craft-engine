package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerClicksCEBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player (right|left) clicks ce block (<'block'>)
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event when the player is clicking the block with a specified item.
    //
    // @Triggers when a player clicks on a CraftEngine custom block.
    // The word "ce" in the event line can also be written as "craftengine".
    // Note that CraftEngine treats left clicks in creative mode as block breaking, so LEFT_CLICK only fires in survival/adventure mode.
    //
    // @Context
    // <context.item> returns the ItemTag the player is clicking with.
    // <context.location> returns the LocationTag the player is clicking on.
    // <context.id> returns the CraftEngine block id of the clicked block, like "default:togglable_light_block".
    // <context.relative> returns a LocationTag of the air block in front of the clicked block.
    // <context.click_type> returns an ElementTag of the click type: LEFT_CLICK or RIGHT_CLICK.
    // <context.hand> returns an ElementTag of the used hand.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate the CraftEngine block's id when a player right clicks any CraftEngine block.
    // on player right clicks ce block:
    // - narrate "You just right-clicked a <context.id>!"
    //
    // @Example
    // # Only fire when a specific CraftEngine block is left clicked.
    // on player left clicks ce block default:togglable_light_block:
    // - narrate "You just left-clicked a togglable light block!"
    // -->

    public PlayerClicksCEBlockScriptEvent() {
        registerCouldMatcher("player (right|left) clicks ce block (<'block'>)");
        registerCouldMatcher("player (right|left) clicks craftengine block (<'block'>)");
        registerSwitches("with");
    }

    public LocationTag location;
    public String id;
    public ItemTag item;
    public CustomBlockInteractEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        int index = path.eventArgLowerAt(1).equals("clicks") ? 1 : 2;
        if (index == 2) {
            CustomBlockInteractEvent.Action action = this.event.action();
            boolean right = action == CustomBlockInteractEvent.Action.RIGHT_CLICK;
            if (path.eventArgLowerAt(1).equals("right") != right) {
                return false;
            }
        }
        String matcher = path.eventArgLowerAt(index + 3);
        if (!matcher.isEmpty() && !matcher.equals("block") && !createMatcher(matcher).doesMatch(this.id)) {
            return false;
        }
        if (!runWithCheck(path, this.item)) {
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
            case "item" -> this.item;
            case "location" -> this.location;
            case "id" -> new ElementTag(this.id);
            case "relative" -> new LocationTag(this.event.bukkitBlock().getRelative(this.event.clickedFace()).getLocation());
            case "click_type" -> new ElementTag(this.event.action());
            case "hand" -> new ElementTag(this.event.hand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerClicksCEBlock(CustomBlockInteractEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.location = new LocationTag(event.location());
        this.id = event.customBlock().id().toString();
        this.item = new ItemTag(event.item());
        this.event = event;
        fire(event);
    }
}
