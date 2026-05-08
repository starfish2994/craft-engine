package net.momirealms.craftengine.core.item.network.encrypt;

public final class NoEncryption implements Encryption {
    public static final Encryption INSTANCE = new NoEncryption();

    private NoEncryption() {}

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
