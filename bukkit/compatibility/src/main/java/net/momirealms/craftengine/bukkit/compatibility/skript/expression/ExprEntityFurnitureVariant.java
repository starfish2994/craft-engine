package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.momirealms.craftengine.bukkit.compatibility.util.FurnitureResolver;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Furniture Variant")
@Description({
        "Get or change the variant of a CraftEngine furniture entity.",
        "Resetting restores the default variant of the furniture."
})
@Example("set furniture variant of target entity to \"oak\"")
@Since("26.8")
public final class ExprEntityFurnitureVariant extends SimplePropertyExpression<Entity, String> {

    public static void register(SkriptAddon addon) {
        DefaultSyntaxInfos.Expression<ExprEntityFurnitureVariant, String> expression = infoBuilder(
                ExprEntityFurnitureVariant.class, String.class,
                "[(custom|ce|craft-engine)] furniture variant",
                "entities",
                false
        ).build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EXPRESSION, expression);
    }

    @Override
    public @Nullable String convert(Entity entity) {
        BukkitFurniture furniture = FurnitureResolver.resolve(entity);
        if (furniture == null) return null;
        return furniture.currentVariant().name();
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(String.class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        for (Entity entity : getExpr().getArray(event)) {
            BukkitFurniture furniture = FurnitureResolver.resolve(entity);
            if (furniture == null) continue;
            String variant = switch (mode) {
                case SET -> {
                    assert delta != null;
                    yield (String) delta[0];
                }
                case RESET -> furniture.config.anyVariantName();
                default -> null;
            };
            if (variant != null) {
                furniture.setVariant(variant, false);
            }
        }
    }

    @Override
    protected String getPropertyName() {
        return "furniture variant";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
