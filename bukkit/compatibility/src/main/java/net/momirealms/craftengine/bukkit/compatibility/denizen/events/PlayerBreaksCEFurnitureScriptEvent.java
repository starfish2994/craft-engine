package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerBreaksCEFurnitureScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks ce furniture <'furniture'>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event when the player is breaking the furniture with a specified item.
    //
    // @Triggers when a player breaks a CraftEngine furniture.
    // The word "ce" in the event line can also be written as "craftengine".
    //
    // @Context
    // <context.location> returns the LocationTag the furniture was broken at.
    // <context.id> returns the CraftEngine furniture id of the furniture that was broken, like "default:wooden_chair".
    // <context.entity> returns the EntityTag of the furniture's base entity.
    // <context.should_drop_items> returns whether the event will drop items.
    //
    // @Determine
    // "NOTHING" to make the furniture drop no items.
    // ListTag(ItemTag) to make the furniture drop a specified list of items.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate the CraftEngine furniture's id when a player breaks any CraftEngine furniture.
    // on player breaks ce furniture furniture:
    // - narrate "You just broke a <context.id>!"
    //
    // @Example
    // # Only fire when a specific CraftEngine furniture is broken with a specific tool.
    // on player breaks ce furniture default:wooden_chair with:diamond_axe:
    // - narrate "You just broke a wooden chair with a diamond axe!"
    // -->

    public PlayerBreaksCEFurnitureScriptEvent() {
        registerCouldMatcher("player breaks ce furniture <'furniture'>");
        registerCouldMatcher("player breaks craftengine furniture <'furniture'>");
        registerSwitches("with");
        this.<PlayerBreaksCEFurnitureScriptEvent>registerTextDetermination("nothing", (evt) -> {
            evt.event.setDropItems(false);
        });
        this.<PlayerBreaksCEFurnitureScriptEvent, ListTag>registerDetermination(null, ListTag.class, (evt, context, list) -> {
            evt.event.setDropItems(false);
            Location location = evt.event.location();
            for (ItemTag newItem : list.filter(ItemTag.class, context)) {
                location.getWorld().dropItemNaturally(location, newItem.getItemStack());
            }
        });
    }

    public LocationTag location;
    public String id;
    public FurnitureBreakEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String matcher = path.eventArgLowerAt(4);
        if (!matcher.equals("furniture") && !createMatcher(matcher).doesMatch(this.id)) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(this.event.getPlayer().getInventory().getItemInMainHand()))) {
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
            case "entity" -> new EntityTag(this.event.furniture().bukkitEntity());
            case "should_drop_items" -> new ElementTag(this.event.dropItems());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerBreaksCEFurniture(FurnitureBreakEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.location = new LocationTag(event.location());
        this.id = event.furniture().id().toString();
        this.event = event;
        fire(event);
    }
}
