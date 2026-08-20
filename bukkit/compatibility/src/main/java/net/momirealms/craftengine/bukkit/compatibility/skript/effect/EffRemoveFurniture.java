package net.momirealms.craftengine.bukkit.compatibility.skript.effect;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.compatibility.util.FurnitureResolver;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Remove Furniture")
@Description({"Removes the furniture bound to the given entities."})
@Example("remove furniture target entity")
@Since("1.0")
public final class EffRemoveFurniture extends Effect {
    private Expression<Entity> entities;

    public static void register(SkriptAddon addon) {
        SyntaxInfo<EffRemoveFurniture> syntaxInfo = SyntaxInfo.builder(EffRemoveFurniture.class)
                .addPattern("remove [(custom|ce|craft-engine)] furniture %entities%")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EFFECT, syntaxInfo);
    }

    @Override
    protected void execute(Event e) {
        for (Entity entity : entities.getArray(e)) {
            Furniture bukkitFurniture = FurnitureResolver.resolve(entity);
            if (bukkitFurniture != null) {
                bukkitFurniture.destroy();
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "remove furniture " + entities.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.entities = (Expression<Entity>) expressions[0];
        return true;
    }
}
