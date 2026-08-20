package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.AbstractDelegatingContext;
import net.momirealms.craftengine.core.plugin.context.ChainParameterSource;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextKey;

import java.util.Optional;

public class ItemBoundAttributeModifier extends DynamicAttributeModifier {
    private final Item item;

    public ItemBoundAttributeModifier(AttributeModifierConfig config, Item item) {
        super(config);
        this.item = item;
    }

    @Override
    public double amount(Context context) {
        return super.amount(new TempItemContext(context, this.item));
    }

    @Override
    public boolean test(Context context) {
        return super.test(new TempItemContext(context, this.item));
    }

    public Item item() {
        return this.item;
    }

    public static class TempItemContext extends AbstractDelegatingContext {
        private final Item item;

        public TempItemContext(Context delegate, Item item) {
            super(delegate);
            this.item = item;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
            ContextKey<Object> parentKey = parameter.parent();
            if (parentKey == null) {
                if (parameter.node().equals("item")) {
                    return (Optional<T>) Optional.of(this.item);
                }
                return super.delegate.contexts().getOptional(parameter);
            }
            Optional<Object> parentValue = getOptionalParameter(parentKey);
            if (parentValue.isEmpty()) {
                return Optional.empty();
            }
            if (parentValue.get() instanceof ChainParameterSource source) {
                return source.getParameter(parameter);
            }
            return Optional.empty();
        }
    }
}
