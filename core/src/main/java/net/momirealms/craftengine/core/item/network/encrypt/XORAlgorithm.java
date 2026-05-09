package net.momirealms.craftengine.core.item.network.encrypt;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class XORAlgorithm implements Algorithm {
    public static final Algorithm INSTANCE = new XORAlgorithm();
    private static final VarHandle LONG_VIEW = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());
    private byte[] keyBytes;
    private long keyLong;

    static {
        INSTANCE.setKey(UUID.randomUUID().toString());
    }

    private XORAlgorithm() {}

    @Override
    public byte[] encrypt(byte[] data) {
        int len = data.length;
        int i = 0;
        int limit = len - 8;
        for (; i <= limit; i += 8) {
            long val = (long) LONG_VIEW.get(data, i);
            LONG_VIEW.set(data, i, val ^ keyLong);
        }
        for (; i < len; i++) {
            data[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }
        return data;
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
            byte[] paddedKey = new byte[8];
            for (int i = 0; i < 8; i++) {
                paddedKey[i] = keyBytes[i % keyBytes.length];
            }
            this.keyLong = (long) LONG_VIEW.get(paddedKey, 0);
        }
    }

}
