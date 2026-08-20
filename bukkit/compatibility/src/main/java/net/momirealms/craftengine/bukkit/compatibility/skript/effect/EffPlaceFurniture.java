package net.momirealms.craftengine.bukkit.compatibility.skript.effect;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Place Furniture")
@Description({"Places furniture at the given locations, optionally with a specific variant."})
@Example("place furniture \"mynamespace:my_furniture\" at target block")
@Example("place furniture \"mynamespace:my_furniture\" with variant \"oak\" at target block")
@Since("1.0")
public final class EffPlaceFurniture extends Effect {
    private Expression<String> furniture;
    @Nullable
    private Expression<String> variants;
    private Expression<Location> locations;

    public static void register(SkriptAddon addon) {
        SyntaxInfo<EffPlaceFurniture> syntaxInfo = SyntaxInfo.builder(EffPlaceFurniture.class)
                .addPattern("place [(custom|ce|craft-engine)] furniture[s] %strings% [with variant %-strings%] [at] [%directions% %locations%]")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EFFECT, syntaxInfo);
    }

    @Override
    protected void execute(Event e) {
        String[] ids = furniture.getArray(e);
        String variant = variants != null ? variants.getSingle(e) : null;
        for (Location location : locations.getArray(e)) {
            for (String id : ids) {
                if (variant != null) {
                    CraftEngineFurniture.place(location, Key.of(id), variant);
                } else {
                    CraftEngineFurniture.place(location, Key.of(id));
                }
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "place furniture " + furniture.toString(event, debug)
                + (variants != null ? " with variant " + variants.toString(event, debug) : "")
                + " " + locations.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        furniture = (Expression<String>) expressions[0];
        variants = (Expression<String>) expressions[1];
        locations = Direction.combine((Expression<? extends Direction>) expressions[2], (Expression<? extends Location>) expressions[3]);
        return true;
    }
}
