package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class TransferFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String server;
    private final String host;
    private final int port;

    private TransferFunction(List<Condition<CTX>> predicates, String server, String host, int port) {
        super(predicates);
        this.server = server;
        this.host = host;
        this.port = port;
    }

    @Override
    protected void runInternal(CTX ctx) {
        Optional<Player> player = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        if (player.isEmpty()) {
            return;
        }
        Player p = player.get();
        if (this.host != null) {
            p.transfer(this.host, this.port);
        } else if (this.server != null) {
            p.transfer(this.server);
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, TransferFunction<CTX>> factory(Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, TransferFunction<CTX>> {

        public Factory(Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public TransferFunction<CTX> create(ConfigSection section) {
            return new TransferFunction<>(
                    getPredicates(section),
                    section.getString("server"),
                    section.getString("host"),
                    section.getInt("port", 25565)
            );
        }
    }
}
