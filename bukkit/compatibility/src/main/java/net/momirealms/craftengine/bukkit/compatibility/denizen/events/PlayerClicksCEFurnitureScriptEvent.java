package net.momirealms.craftengine.bukkit.compatibility.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.momirealms.craftengine.bukkit.api.event.FurnitureHitEvent;
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerClicksCEFurnitureScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player (right|left) clicks ce furniture (<'furniture'>)
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event when the player is clicking the furniture with a specified item.
    //
    // @Triggers when a player clicks on a CraftEngine furniture.
    // The word "ce" in the event line can also be written as "craftengine".
    //
    // @Context
    // <context.item> returns the ItemTag the player is clicking with.
    // <context.location> returns the LocationTag of the clicked furniture.
    // <context.id> returns the CraftEngine furniture id of the clicked furniture, like "default:wooden_chair".
    // <context.entity> returns the EntityTag of the furniture's base entity.
    // <context.click_type> returns an ElementTag of the click type: LEFT_CLICK or RIGHT_CLICK.
    // <context.hand> returns an ElementTag of the used hand.
    //
    // @Player Always.
    //
    // @Example
    // # Narrate the CraftEngine furniture's id when a player right clicks any CraftEngine furniture.
    // on player right clicks ce furniture:
    // - narrate "You just right-clicked a <context.id>!"
    //
    // @Example
    // # Only fire when a specific CraftEngine furniture is clicked.
    // on player clicks ce furniture default:wooden_chair:
    // - narrate "You just clicked a wooden chair!"
    // -->

    public PlayerClicksCEFurnitureScriptEvent() {
        registerCouldMatcher("player (right|left) clicks ce furniture (<'furniture'>)");
        registerCouldMatcher("player (right|left) clicks craftengine furniture (<'furniture'>)");
        registerSwitches("with");
    }

    public LocationTag location;
    public String id;
    public ItemTag item;
    public EntityTag entity;
    public ElementTag clickType;
    public ElementTag hand;
    public Player player;
    public Cancellable event;

    @Override
    public boolean matches(ScriptPath path) {
        int index = path.eventArgLowerAt(1).equals("clicks") ? 1 : 2;
        if (index == 2) {
            boolean right = this.clickType.asString().equals("RIGHT_CLICK");
            if (path.eventArgLowerAt(1).equals("right") != right) {
                return false;
            }
        }
        String matcher = path.eventArgLowerAt(index + 3);
        if (!matcher.isEmpty() && !matcher.equals("furniture") && !createMatcher(matcher).doesMatch(this.id)) {
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
        return new BukkitScriptEntryData(this.player);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> this.item;
            case "location" -> this.location;
            case "id" -> new ElementTag(this.id);
            case "entity" -> this.entity;
            case "click_type" -> this.clickType;
            case "hand" -> this.hand;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerRightClicksCEFurniture(FurnitureInteractEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        EquipmentSlot slot = event.hand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
        ItemStack itemStack = event.getPlayer().getInventory().getItem(slot);
        this.location = new LocationTag(event.location());
        this.id = event.furniture().id().toString();
        this.item = new ItemTag(itemStack);
        this.entity = new EntityTag(event.furniture().bukkitEntity());
        this.clickType = new ElementTag("RIGHT_CLICK");
        this.hand = new ElementTag(slot);
        this.player = event.getPlayer();
        this.event = event;
        fire(event);
    }

    @EventHandler
    public void onPlayerLeftClicksCEFurniture(FurnitureHitEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.location = new LocationTag(event.location());
        this.id = event.furniture().id().toString();
        this.item = new ItemTag(event.getPlayer().getInventory().getItemInMainHand());
        this.entity = new EntityTag(event.furniture().bukkitEntity());
        this.clickType = new ElementTag("LEFT_CLICK");
        this.hand = new ElementTag(EquipmentSlot.HAND);
        this.player = event.getPlayer();
        this.event = event;
        fire(event);
    }
}
