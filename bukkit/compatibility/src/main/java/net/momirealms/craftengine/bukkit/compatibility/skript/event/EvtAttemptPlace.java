package net.momirealms.craftengine.bukkit.compatibility.skript.event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockAttemptPlaceEvent;
import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptPlaceEvent;
import net.momirealms.craftengine.core.block.UnsafeBlockStateMatcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
@Name("On Custom Block/Furniture Attempt Place")
@Description({"Fires when a player attempts to place a custom block or furniture, before it is actually placed."})
@Example("""
        on attempt to place custom block:
            cancel event
        """)
@Since("26.8")
public final class EvtAttemptPlace extends SkriptEvent {
    @Nullable
    private Literal<?> types;
    private UnsafeBlockStateMatcher[] blockArray;
    private List<String> idList;

    public static void register(SkriptAddon addon) {
        SyntaxRegistry syntaxRegistry = addon.registry(SyntaxRegistry.class);
        EventValueRegistry valueRegistry = addon.registry(EventValueRegistry.class);

        BukkitSyntaxInfos.Event<EvtAttemptPlace> blockEvent = BukkitSyntaxInfos.Event.builder(EvtAttemptPlace.class, "Attempt Place Custom Block")
                .addPattern("attempt[ing] to place (custom|ce|craft-engine) block[s] [[of] %-unsafeblockstatematchers%]")
                .addDescription("Called when a player attempts to place a custom block, before it is placed. Cancellable.")
                .addEvent(CustomBlockAttemptPlaceEvent.class)
                .build();
        syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, blockEvent);

        valueRegistry.register(EventValue.builder(CustomBlockAttemptPlaceEvent.class, Location.class).getter(CustomBlockAttemptPlaceEvent::location).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(CustomBlockAttemptPlaceEvent.class, Player.class).getter(CustomBlockAttemptPlaceEvent::player).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(CustomBlockAttemptPlaceEvent.class, Block.class).getter(CustomBlockAttemptPlaceEvent::clickedBlock).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(CustomBlockAttemptPlaceEvent.class, World.class).getter(e -> e.location().getWorld()).time(EventValue.Time.NOW).build());

        BukkitSyntaxInfos.Event<EvtAttemptPlace> furnitureEvent = BukkitSyntaxInfos.Event.builder(EvtAttemptPlace.class, "Attempt Place Furniture")
                .addPattern("attempt[ing] to place [(custom|ce|craft-engine)] furniture[s] [[of] %-strings%]")
                .addDescription("Called when a player attempts to place a furniture, before it is placed. Cancellable.")
                .addEvent(FurnitureAttemptPlaceEvent.class)
                .build();
        syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, furnitureEvent);

        valueRegistry.register(EventValue.builder(FurnitureAttemptPlaceEvent.class, Location.class).getter(FurnitureAttemptPlaceEvent::location).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureAttemptPlaceEvent.class, Player.class).getter(FurnitureAttemptPlaceEvent::player).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureAttemptPlaceEvent.class, Block.class).getter(FurnitureAttemptPlaceEvent::clickedBlock).time(EventValue.Time.NOW).build());
        valueRegistry.register(EventValue.builder(FurnitureAttemptPlaceEvent.class, World.class).getter(e -> e.location().getWorld()).time(EventValue.Time.NOW).build());
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
        if (args[0] != null) {
            types = args[0];
            if (matchedPattern == 0) {
                blockArray = ((Literal<UnsafeBlockStateMatcher>) types).getAll();
            } else {
                idList = Arrays.stream(((Literal<String>) types).getAll()).toList();
            }
        }
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (types == null) return true;
        if (event instanceof CustomBlockAttemptPlaceEvent attemptPlaceEvent && blockArray != null) {
            return Arrays.stream(blockArray).anyMatch(block -> block.matches(attemptPlaceEvent.blockState()));
        } else if (event instanceof FurnitureAttemptPlaceEvent attemptPlaceEvent && idList != null) {
            return idList.contains(attemptPlaceEvent.furniture().id().toString());
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "attempt to place" + (types != null ? " of " + types.toString(event, debug) : "");
    }
}
