package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeSide;
import net.momirealms.craftengine.core.attribute.damage.EntityDamageContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeValueTag extends StaticTagResolver implements StringTag {
    public static final AttributeValueTag ATTACKER = new AttributeValueTag("attacker_attr", AttributeSide.ATTACKER);
    public static final AttributeValueTag VICTIM = new AttributeValueTag("victim_attr", AttributeSide.VICTIM);

    private final AttributeSide side;

    private AttributeValueTag(String name, AttributeSide side) {
        super(name);
        this.side = side;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        if (!(ctx.target() instanceof EntityDamageContext damageContext)) {
            return null;
        }
        String first = arguments.popOr("No attribute id provided").toString();
        Key key = arguments.hasNext() ? Key.of(first, arguments.pop().toString()) : Key.of(first);
        Attribute attribute = CraftEngine.instance().attributeManager().getAttribute(key)
                .orElseThrow(() -> ctx.newException("Unknown attribute: " + key.asString(), arguments));
        return Tag.selfClosingInserting(Component.text(String.valueOf(damageContext.event().getAttributeValue(this.side, attribute))));
    }

    @Nullable
    @Override
    public Object resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        return resolveValue(lookup(args), context);
    }

    @Override
    public StringTag precompile(String[] args) {
        final Attribute attribute = lookup(args);
        return (boundArgs, context) -> resolveValue(attribute, context);
    }

    private Attribute lookup(String[] args) {
        StringTag.requireArg(args, 0, "No attribute id provided");
        Key key = args.length == 1 ? Key.of(args[0]) : Key.of(args[0], args[1]);
        return CraftEngine.instance().attributeManager().getAttribute(key).orElseThrow(() -> new IllegalArgumentException("Unknown attribute: " + key.asString()));
    }

    private Object resolveValue(Attribute attribute, net.momirealms.craftengine.core.plugin.context.Context context) {
        if (!(context instanceof EntityDamageContext damageContext)) {
            return null;
        }
        return damageContext.event().getAttributeValue(this.side, attribute);
    }
}
