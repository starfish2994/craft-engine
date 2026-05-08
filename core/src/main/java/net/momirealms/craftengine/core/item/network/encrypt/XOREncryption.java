package net.momirealms.craftengine.core.item.network.encrypt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class XOREncryption implements Encryption {
    public static final Encryption INSTANCE = new XOREncryption();
    private byte[] keyBytes;
    private int keyLength;

    static {
        INSTANCE.setKey(UUID.randomUUID().toString());
    }

    private XOREncryption() {}

    @Override
    public byte[] encrypt(byte[] data) {
        int length = data.length;
        byte[] result = new byte[length];
        for (int i = 0, j = 0; i < length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[j]);
            j++;
            if (j == keyLength) {
                j = 0;
            }
        }
        return result;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return encrypt(data);
    }

    @Override
    public void setKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        } else {
            this.keyBytes = key.getBytes(StandardCharsets.UTF_8);
            this.keyLength = keyBytes.length;
        }
    }

}
