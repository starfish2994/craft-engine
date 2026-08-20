package net.momirealms.craftengine.bukkit.compatibility.denizen.commands;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.compatibility.util.FurnitureResolver;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;

import java.util.List;

// ScriptEntry 的对象存取 API 在 Denizen 开发版快照中被标记 @Deprecated（软废弃），
// 但 Denizen 官方插件源码仍全面使用该 API，此处与官方写法保持一致
@SuppressWarnings("deprecation")
public class RemoveCEFurnitureCommand extends AbstractCommand implements Holdable {

    public RemoveCEFurnitureCommand() {
        setName("removecefurniture");
        setSyntax("removecefurniture [<entity>|...]");
        setRequiredArguments(1, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name RemoveCEFurniture
    // @Syntax removecefurniture [<entity>|...]
    // @Required 1
    // @Maximum 1
    // @Short Removes CraftEngine furniture.
    // @Group world
    // @Plugin CraftEngine
    //
    // @Description
    // Removes CraftEngine furniture from the world, without dropping loot or playing effects.
    //
    // Entities that are not part of a CraftEngine furniture are ignored.
    //
    // The removecefurniture command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <EntityTag.is_ce_furniture>
    // <EntityTag.ce_id>
    //
    // @Usage
    // Use to remove the furniture the player is looking at.
    // - removecefurniture <player.precise_target>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Missing entity argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        final ListTag entities = scriptEntry.getObjectTag("entities");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entities);
        }
        List<EntityTag> entityList = entities.filter(EntityTag.class, scriptEntry);
        for (EntityTag entityTag : entityList) {
            BukkitFurniture furniture = FurnitureResolver.resolve(entityTag.getBukkitEntity());
            if (furniture == null) {
                Debug.echoDebug(scriptEntry, "Entity " + entityTag + " is not a CraftEngine furniture, skipped.");
                continue;
            }
            CraftEngineFurniture.remove(furniture, false, false);
        }
        scriptEntry.setFinished(true);
    }
}
