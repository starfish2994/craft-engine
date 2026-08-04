package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("All Custom Block States")
@Description({"Get all possible states of custom blocks."})
@Example("""
        loop all states of custom block "mynamespace:lamp":
            send "%loop-value%"
        """)
@Since("26.8")
public final class ExprBlockCustomBlockStates extends SimpleExpression<ImmutableBlockState> {
    private Expression<String> blockIds;

    public static void register(SkriptAddon addon) {
        DefaultSyntaxInfos.Expression<ExprBlockCustomBlockStates, ImmutableBlockState> expression = DefaultSyntaxInfos.Expression.builder(ExprBlockCustomBlockStates.class, ImmutableBlockState.class)
                .addPattern("[(all|the)] [possible] [block] states of (custom|ce|craft-engine) block[s] %strings%")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EXPRESSION, expression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        blockIds = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected ImmutableBlockState[] get(Event event) {
        List<ImmutableBlockState> states = new ArrayList<>();
        for (String id : blockIds.getArray(event)) {
            BlockDefinition definition = CraftEngineBlocks.byId(Key.of(id));
            if (definition == null) continue;
            states.addAll(definition.getPossibleStates(new CompoundTag()));
        }
        return states.toArray(new ImmutableBlockState[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ImmutableBlockState> getReturnType() {
        return ImmutableBlockState.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "all states of custom block " + blockIds.toString(event, debug);
    }
}
