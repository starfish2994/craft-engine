package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Furniture Variants")
@Description({"Get all variant names of furniture definitions."})
@Example("""
        loop variants of furniture "mynamespace:chair":
            send "%loop-value%"
        """)
@Since("26.8")
public final class ExprFurnitureVariants extends SimpleExpression<String> {
    private Expression<String> furnitureIds;

    public static void register(SkriptAddon addon) {
        DefaultSyntaxInfos.Expression<ExprFurnitureVariants, String> expression = DefaultSyntaxInfos.Expression.builder(ExprFurnitureVariants.class, String.class)
                .addPattern("[(all|the)] variants of [(custom|ce|craft-engine)] furniture %strings%")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EXPRESSION, expression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        furnitureIds = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        List<String> variants = new ArrayList<>();
        for (String id : furnitureIds.getArray(event)) {
            FurnitureDefinition definition = CraftEngineFurniture.byId(Key.of(id));
            if (definition == null) continue;
            variants.addAll(definition.variants().keySet());
        }
        return variants.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "variants of furniture " + furnitureIds.toString(event, debug);
    }
}
