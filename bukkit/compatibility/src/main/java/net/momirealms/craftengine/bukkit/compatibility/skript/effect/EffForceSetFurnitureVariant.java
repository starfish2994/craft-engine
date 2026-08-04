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
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Force Set Furniture Variant")
@Description({
        "Forcefully sets the variant of furniture entities, skipping the collision check.",
        "Compared to 'set furniture variant of ... to ...' which respects collision checks."
})
@Example("force set furniture variant of target entity to \"ceiling\"")
@Since("26.8")
public final class EffForceSetFurnitureVariant extends Effect {
    private Expression<Entity> entities;
    private Expression<String> variants;

    public static void register(SkriptAddon addon) {
        SyntaxInfo<EffForceSetFurnitureVariant> syntaxInfo = SyntaxInfo.builder(EffForceSetFurnitureVariant.class)
                .addPattern("force[fully] set [(custom|ce|craft-engine)] furniture variant of %entities% to %strings%")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EFFECT, syntaxInfo);
    }

    @Override
    protected void execute(Event e) {
        String variant = variants.getSingle(e);
        if (variant == null) return;
        for (Entity entity : entities.getArray(e)) {
            BukkitFurniture furniture = FurnitureResolver.resolve(entity);
            if (furniture != null) {
                furniture.setVariant(variant, true);
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "force set furniture variant of " + entities.toString(event, debug) + " to " + variants.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        entities = (Expression<Entity>) expressions[0];
        variants = (Expression<String>) expressions[1];
        return true;
    }
}
