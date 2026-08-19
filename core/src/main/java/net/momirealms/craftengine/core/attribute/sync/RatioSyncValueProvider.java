package net.momirealms.craftengine.core.attribute.sync;

public final class RatioSyncValueProvider implements SyncValueProvider {
    public static final RatioSyncValueProvider INSTANCE = new RatioSyncValueProvider();
    public static final SyncValueProviderFactory<RatioSyncValueProvider> FACTORY = args -> INSTANCE;

    private RatioSyncValueProvider() {
    }

    @Override
    public double resolve(double value, double base) {
        return value / base;
    }
}
