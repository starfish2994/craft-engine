package net.momirealms.craftengine.bukkit.compatibility.skript.condition;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Furniture Seat")
@Description({"Checks if the entity is a seat of a CraftEngine furniture."})
@Example("if target entity is a furniture seat:")
@Since("26.8")
public final class CondIsFurnitureSeat extends Condition {
    private Expression<Entity> entities;

    public static void register(SkriptAddon addon) {
        SyntaxInfo<CondIsFurnitureSeat> condition = SyntaxInfo.builder(CondIsFurnitureSeat.class)
                .addPattern("%entities% (is|are) [a[n]] [(custom|ce|craft-engine)] furniture seat[s]")
                .addPattern("%entities% (is|are) (n't| not) [a[n]] [(custom|ce|craft-engine)] furniture seat[s]")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.CONDITION, condition);
    }

    @Override
    public boolean check(Event event) {
        return entities.check(event, CraftEngineFurniture::isSeat, isNegated());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, entities, "furniture seat");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        entities = (Expression<Entity>) expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }
}
