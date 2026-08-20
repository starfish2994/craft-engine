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
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Remove Custom Block")
@Description({"Removes custom blocks at the given locations. Non-custom blocks are ignored."})
@Example("remove custom block at target block")
@Since("26.8")
public final class EffRemoveCustomBlock extends Effect {
    private Expression<Location> locations;

    public static void register(SkriptAddon addon) {
        SyntaxInfo<EffRemoveCustomBlock> syntaxInfo = SyntaxInfo.builder(EffRemoveCustomBlock.class)
                .addPattern("(remove|break) (custom|ce|craft-engine) block[s] [at] [%directions% %locations%]")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EFFECT, syntaxInfo);
    }

    @Override
    protected void execute(Event e) {
        Player player = e instanceof PlayerEvent playerEvent ? playerEvent.getPlayer() : null;
        for (Location location : locations.getArray(e)) {
            CraftEngineBlocks.remove(location.getBlock(), player, false, false, true);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "remove custom block " + locations.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        locations = Direction.combine((Expression<? extends Direction>) expressions[0], (Expression<? extends Location>) expressions[1]);
        return true;
    }
}
