package net.momirealms.craftengine.core.item.network.encrypt;

public final class NoAlgorithm implements Algorithm {
    public static final Algorithm INSTANCE = new NoAlgorithm();

    private NoAlgorithm() {}

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data;
    }

    @Override
    public void setKey(String key) {
    }

}
