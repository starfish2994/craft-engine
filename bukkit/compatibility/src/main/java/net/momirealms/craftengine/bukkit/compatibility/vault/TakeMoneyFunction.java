package net.momirealms.craftengine.bukkit.compatibility.vault;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.AbstractConditionalFunction;
import net.momirealms.craftengine.core.plugin.context.function.FunctionFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import org.bukkit.entity.Player;

import java.util.List;

public final class TakeMoneyFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final NumberProvider amount;

    private TakeMoneyFunction(List<Condition<CTX>> predicates,
                              PlayerSelector<CTX> selector,
                              NumberProvider amount) {
        super(predicates);
        this.selector = selector;
        this.amount = amount;
    }

    @Override
    protected void runInternal(CTX ctx) {
        if (this.selector != null) {
            this.selector.get(ctx).forEach(p -> VaultUtils.withdraw((Player) p.platformPlayer(), this.amount.getDouble(ctx)));
        } else {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER)
                    .ifPresent(it -> VaultUtils.withdraw((Player) it.platformPlayer(), this.amount.getDouble(ctx)));
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, TakeMoneyFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, TakeMoneyFunction<CTX>> {
        private static final String[] AMOUNT = ConfigKeys.of("amount|value");

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public TakeMoneyFunction<CTX> create(ConfigSection section) {
            return new TakeMoneyFunction<>(
                    getPredicates(section),
                    getPlayerSelector(section),
                    section.getNonNullNumber(AMOUNT)
            );
        }
    }
}
