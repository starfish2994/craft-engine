package net.momirealms.craftengine.bukkit.compatibility.vault;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class HasMoneyCondition<CTX extends Context> implements Condition<CTX> {
    private final NumberProvider amount;

    private HasMoneyCondition(NumberProvider amount) {
        this.amount = amount;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<net.momirealms.craftengine.core.entity.player.Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        return optionalPlayer
                .map(player -> VaultUtils.has((Player) player.platformPlayer(), this.amount.getDouble(ctx)))
                .orElse(false);
    }

    public static <CTX extends Context> ConditionFactory<CTX, HasMoneyCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, HasMoneyCondition<CTX>> {
        private static final String[] AMOUNT = ConfigKeys.of("amount|value");

        @Override
        public HasMoneyCondition<CTX> create(ConfigSection section) {
            return new HasMoneyCondition<>(section.getNonNullNumber(AMOUNT));
        }
    }
}
