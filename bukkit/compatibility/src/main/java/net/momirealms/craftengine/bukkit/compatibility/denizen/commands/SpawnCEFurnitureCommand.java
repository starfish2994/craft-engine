package net.momirealms.craftengine.bukkit.compatibility.denizen.commands;

import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.EllipsoidTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// ScriptEntry 的对象存取 API 在 Denizen 开发版快照中被标记 @Deprecated（软废弃），
// 但 Denizen 官方插件源码仍全面使用该 API，此处与官方写法保持一致
@SuppressWarnings("deprecation")
public class SpawnCEFurnitureCommand extends AbstractCommand implements Holdable {

    public SpawnCEFurnitureCommand() {
        setName("spawncefurniture");
        setSyntax("spawncefurniture [<location>|.../<ellipsoid>/<cuboid>] [<craftengine_furniture>|...] (direction:north/east/south/west) (<percent chance>|...)");
        setRequiredArguments(2, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name SpawnCEFurniture
    // @Syntax spawncefurniture [<location>|.../<ellipsoid>/<cuboid>] [<craftengine_furniture>|...] (direction:north/east/south/west) (<percent chance>|...)
    // @Required 2
    // @Maximum 4
    // @Short Spawns CraftEngine furniture.
    // @Group world
    // @Plugin CraftEngine
    //
    // @Description
    // Spawns CraftEngine furniture in the world based on the criteria given.
    //
    // Furniture is specified as CraftEngine furniture ids, like "custom:oak_lamp".
    //
    // Optionally specify 'direction' to control the direction the furniture faces, one of north/east/south/west.
    //
    // Specify (<percent chance>|...) to give a chance of each type of furniture being spawned (in any furniture at all).
    // Chances are rolled in order per location until one succeeds; if no roll succeeds, nothing is spawned at that location.
    //
    // Note that specifying a list of locations will take more time in parsing than in the actual spawning.
    //
    // The spawncefurniture command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <EntityTag.is_ce_furniture>
    // <EntityTag.ce_id>
    //
    // @Usage
    // Use to spawn a furniture where the player is looking.
    // - spawncefurniture <player.cursor_on> custom:oak_lamp
    //
    // @Usage
    // Use to spawn a furniture facing west, and wait for completion.
    // - ~spawncefurniture <player.cursor_on> custom:oak_lamp direction:west
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(CuboidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && (arg.startsWith("cu@") || !arg.getRawValue().contains("|"))) {
                scriptEntry.addObject("locations", arg.asType(CuboidTag.class).getBlockLocationsUnfiltered(false));
            }
            else if (arg.matchesArgumentType(EllipsoidTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")
                    && (arg.startsWith("ellipsoid@") || !arg.getRawValue().contains("|"))) {
                scriptEntry.addObject("locations", arg.asType(EllipsoidTag.class).getBlockLocationsUnfiltered(false));
            }
            else if (arg.matchesArgumentList(LocationTag.class)
                    && !scriptEntry.hasObject("locations")
                    && !scriptEntry.hasObject("location_list")) {
                scriptEntry.addObject("location_list", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("direction")
                    && arg.matchesPrefix("direction", "d")) {
                scriptEntry.addObject("direction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("furniture")) {
                scriptEntry.addObject("furniture", PlaceCEBlockCommand.listArg(arg));
            }
            else if (!scriptEntry.hasObject("percents")) {
                scriptEntry.addObject("percents", PlaceCEBlockCommand.listArg(arg));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("furniture")) {
            throw new InvalidArgumentsException("Missing CraftEngine furniture argument!");
        }
        if (!scriptEntry.hasObject("locations") && !scriptEntry.hasObject("location_list")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        final ListTag furniture = scriptEntry.getObjectTag("furniture");
        ListTag percents = scriptEntry.getObjectTag("percents");
        final ElementTag directionElement = scriptEntry.getElement("direction");
        if (percents != null && percents.size() != furniture.size()) {
            Debug.echoError(scriptEntry, "Percents length != furniture length");
            percents = null;
        }
        List<FurnitureDefinition> definitions = new ArrayList<>(furniture.size());
        for (String furnitureId : furniture) {
            FurnitureDefinition definition = CraftEngineFurniture.byId(Key.of(furnitureId));
            if (definition == null) {
                Debug.echoError(scriptEntry, "Invalid CraftEngine furniture '" + furnitureId + "'");
                scriptEntry.setFinished(true);
                return;
            }
            definitions.add(definition);
        }
        Float yaw = null;
        if (directionElement != null) {
            yaw = switch (directionElement.asString().toLowerCase(Locale.ROOT)) {
                case "north" -> 180f;
                case "east" -> -90f;
                case "south" -> 0f;
                case "west" -> 90f;
                default -> null;
            };
            if (yaw == null) {
                Debug.echoError(scriptEntry, "Invalid direction '" + directionElement + "', expected one of north/east/south/west");
                scriptEntry.setFinished(true);
                return;
            }
        }
        List<Float> percentages = null;
        if (percents != null) {
            percentages = new ArrayList<>(percents.size());
            for (String str : percents) {
                percentages.add(new ElementTag(str).asFloat());
            }
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), furniture, directionElement, percents,
                    scriptEntry.hasObject("locations") ? db("locations", scriptEntry.getObject("locations")) : scriptEntry.getObjectTag("location_list"));
        }
        List<LocationTag> locations = PlaceCEBlockCommand.getLocations(scriptEntry);
        if (locations == null || locations.isEmpty()) {
            scriptEntry.setFinished(true);
            return;
        }
        int index = 0;
        for (LocationTag location : locations) {
            if (location == null || location.getWorld() == null) {
                Debug.echoError(scriptEntry, "Input is not a valid LocationTag");
                scriptEntry.setFinished(true);
                return;
            }
            spawnAt(location, index, definitions, percentages, yaw);
            index++;
        }
        scriptEntry.setFinished(true);
    }

    static void spawnAt(LocationTag locationTag, int index, List<FurnitureDefinition> definitions, List<Float> percents, Float yaw) {
        FurnitureDefinition definition;
        if (percents == null) {
            definition = definitions.get(index % definitions.size());
        }
        else {
            definition = null;
            for (int i = 0; i < definitions.size(); i++) {
                float percent = percents.get(i) / 100f;
                if (CoreUtilities.getRandom().nextDouble() <= percent) {
                    definition = definitions.get(i);
                    break;
                }
            }
            if (definition == null) {
                return;
            }
        }
        Location location = locationTag.clone();
        if (yaw != null) {
            location.setYaw(yaw);
        }
        CraftEngineFurniture.place(location, definition, definition.anyVariantName(), true);
    }
}
