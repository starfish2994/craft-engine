package net.momirealms.craftengine.core.attribute.sync;

public final class DeltaSyncValueProvider implements SyncValueProvider {
    public static final DeltaSyncValueProvider INSTANCE = new DeltaSyncValueProvider();
    public static final SyncValueProviderFactory<DeltaSyncValueProvider> FACTORY = args -> INSTANCE;

    private DeltaSyncValueProvider() {
    }

    @Override
    public double resolve(double value, double base) {
        return value - base;
    }
}
