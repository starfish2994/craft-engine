package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.momirealms.craftengine.bukkit.compatibility.util.FurnitureResolver;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Furniture ID")
@Description({"Get the CraftEngine furniture id of an entity."})
@Example("set {_id} to furniture id of target entity")
@Since("1.0")
public final class ExprEntityFurnitureID extends SimplePropertyExpression<Object, String> {

    public static void register(SkriptAddon addon) {
        DefaultSyntaxInfos.Expression<ExprEntityFurnitureID, String> expression = infoBuilder(
                ExprEntityFurnitureID.class, String.class,
                "[(custom|ce|craft-engine)] furniture [namespace] id",
                "entities",
                false
        ).build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EXPRESSION, expression);
    }

    @Override
    public @Nullable String convert(Object object) {
        if (object instanceof Entity entity) {
            BukkitFurniture furniture = FurnitureResolver.resolve(entity);
            return furniture == null ? null : furniture.id().toString();
        }
        return null;
    }

    @Override
    protected String getPropertyName() {
        return "furniture id";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
