package net.momirealms.craftengine.bukkit.compatibility.skript.event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import net.momirealms.craftengine.bukkit.api.event.FurnitureHitEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unchecked"})
@Name("On Furniture Hit")
@Description({"Fires when a player hits a furniture."})
@Example("""
        on hit of furniture "mynamespace:my_furniture":
            cancel event
        """)
@Since("26.8")
public final class EvtFurnitureHit extends SkriptEvent {
    @Nullable
    private Literal<String> ids;
    private List<String> idList;

    public static void register(SkriptAddon addon) {
        SyntaxRegistry syntaxRegistry = addon.registry(SyntaxRegistry.class);
        EventValueRegistry valueRegistry = addon.registry(EventValueRegistry.class);

        BukkitSyntaxInfos.Event<EvtFurnitureHit> hitEvent = BukkitSyntaxInfos.Event.builder(EvtFurnitureHit.class, "Hit Furniture")
                .addPattern("hit[ting] [of] [(custom|ce|craft-engine)] furniture[s] [[of] %-strings%]")
                .addDescription("Called when a player hits a furniture. Cancellable.")
                .addEvent(FurnitureHitEvent.class)
                .build();
        syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, hitEvent);

        valueRegistry.register(EventValue.builder(FurnitureHitEvent.class, Location.class).getter(FurnitureHitEvent::location).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureHitEvent.class, Player.class).getter(FurnitureHitEvent::player).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureHitEvent.class, Entity.class).getter(e -> e.furniture().bukkitEntity()).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureHitEvent.class, World.class).getter(e -> e.location().getWorld()).time(EventValue.Time.NOW).build());
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
        if (args[0] != null) {
            ids = ((Literal<String>) args[0]);
            idList = Arrays.stream(ids.getAll()).toList();
        }
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof FurnitureHitEvent hitEvent)) return false;
        if (ids == null) return true;
        return idList.contains(hitEvent.furniture().id().toString());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "hit furniture" + (ids != null ? " of " + ids.toString(event, debug) : "");
    }
}
