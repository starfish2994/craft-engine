package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.ExistingBlock;

import java.util.*;

public class MatchBlockCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> ids;
    private final boolean regexMatch;

    public MatchBlockCondition(Collection<String> ids, boolean regexMatch) {
        this.ids = new HashSet<>(ids);
        this.regexMatch = regexMatch;
    }

    @Override
    public Key type() {
        return CommonConditions.MATCH_BLOCK;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<ExistingBlock> block = ctx.getOptionalParameter(DirectContextParameters.BLOCK);
        return block.filter(blockInWorld -> MiscUtils.matchRegex(blockInWorld.id().asString(), this.ids, this.regexMatch)).isPresent();
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            List<String> ids = MiscUtils.getAsStringList(arguments.get("id"));
            if (ids.isEmpty()) {
                throw new LocalizedResourceConfigException("warning.config.condition.match_block.missing_id");
            }
            boolean regex = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("regex", false), "regex");
            return new MatchBlockCondition<>(ids, regex);
        }
    }
}
