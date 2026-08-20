package net.momirealms.craftengine.core.attribute;

public final class EmptyAttributeHolder implements AttributeGetter {
    public static final EmptyAttributeHolder INSTANCE = new EmptyAttributeHolder();

    @Override
    public double getAttributeValue(Attribute attribute) {
        return 0;
    }
}
