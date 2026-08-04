package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.property.Property;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Custom Block State Property")
@Description({
        "Get or change a single property of a custom block state.",
        "Block states are immutable: setting a property replaces the state stored in the variable."
})
@Example("""
        set {_s} to "mynamespace:pillar" parsed as custom block state
        set custom block property "axis" of {_s} to "x"
        place custom block {_s} at target block
        """)
@Since("26.8")
public final class ExprBlockCustomBlockStateProperty extends SimpleExpression<String> {
    private Expression<String> propertyNames;
    private Expression<ImmutableBlockState> states;

    public static void register(SkriptAddon addon) {
        DefaultSyntaxInfos.Expression<ExprBlockCustomBlockStateProperty, String> expression = DefaultSyntaxInfos.Expression.builder(ExprBlockCustomBlockStateProperty.class, String.class)
                .addPattern("(custom|ce|craft-engine) block [state] propert(y|ies) %strings% of %customblockstates%")
                .addPattern("%customblockstates%'[s] (custom|ce|craft-engine) block [state] propert(y|ies) %strings%")
                .build();
        addon.registry(SyntaxRegistry.class).register(SyntaxRegistry.EXPRESSION, expression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            propertyNames = (Expression<String>) exprs[0];
            states = (Expression<ImmutableBlockState>) exprs[1];
        } else {
            states = (Expression<ImmutableBlockState>) exprs[0];
            propertyNames = (Expression<String>) exprs[1];
        }
        return true;
    }

    @Override
    protected String[] get(Event event) {
        List<String> values = new ArrayList<>();
        for (ImmutableBlockState state : states.getArray(event)) {
            for (String name : propertyNames.getArray(event)) {
                Property<?> property = state.getProperty(name);
                if (property == null) continue;
                Comparable<?> value = state.getNullable(property);
                if (value == null) continue;
                values.add(Property.formatValue(property, value));
            }
        }
        return values.toArray(new String[0]);
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) {
            return CollectionUtils.array(String.class);
        }
        return null;
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0) return;
        String valueName = (String) delta[0];
        List<ImmutableBlockState> newStates = new ArrayList<>();
        for (ImmutableBlockState state : states.getArray(event)) {
            ImmutableBlockState newState = state;
            for (String name : propertyNames.getArray(event)) {
                Property<?> property = newState.getProperty(name);
                if (property == null) continue;
                Object value = property.valueByName(valueName);
                if (value == null) continue;
                newState = ImmutableBlockState.with(newState, property, value);
            }
            newStates.add(newState);
        }
        states.change(event, newStates.toArray(new ImmutableBlockState[0]), Changer.ChangeMode.SET);
    }

    @Override
    public boolean isSingle() {
        return states.isSingle() && propertyNames.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "custom block state property " + propertyNames.toString(event, debug) + " of " + states.toString(event, debug);
    }
}
