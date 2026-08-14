package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;
import java.util.Optional;

public final class SetFurnitureVariantFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String variantName;
    private final boolean force;

    private SetFurnitureVariantFunction(List<Condition<CTX>> predicates, String variantName, boolean force) {
        super(predicates);
        this.variantName = variantName;
        this.force = force;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<Furniture> furnitureOptional = ctx.getOptionalParameter(DirectContextParameters.FURNITURE);
        furnitureOptional.ifPresent(furniture -> furniture.setVariant(this.variantName, this.force));
    }

    public static <CTX extends Context> FunctionFactory<CTX, SetFurnitureVariantFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, SetFurnitureVariantFunction<CTX>> {
        private static final String[] VARIANT = ConfigKeys.of("variant|anchor_type");

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public SetFurnitureVariantFunction<CTX> create(ConfigSection section) {

            return new SetFurnitureVariantFunction<>(
                    getPredicates(section),
                    section.getNonNullString(VARIANT),
                    section.getBoolean("force", true)
            );
        }
    }
}