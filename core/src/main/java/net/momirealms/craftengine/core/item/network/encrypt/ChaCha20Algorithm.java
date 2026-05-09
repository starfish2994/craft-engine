package net.momirealms.craftengine.core.item.network.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class ChaCha20Algorithm implements Algorithm {
    public static final Algorithm INSTANCE = new ChaCha20Algorithm();
    private static final int KEY_LENGTH = 32;
    private static final int NONCE_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final byte[] INTERNAL_KEY = generateKey();
    private static final ThreadLocal<Cipher> ENCRYPT_CIPHER =
            ThreadLocal.withInitial(() -> {
                try {
                    return Cipher.getInstance("ChaCha20-Poly1305");
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            });

    private static final ThreadLocal<Cipher> DECRYPT_CIPHER =
            ThreadLocal.withInitial(() -> {
                try {
                    return Cipher.getInstance("ChaCha20-Poly1305");
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            });
    private static SecretKeySpec KEY_SPEC = new SecretKeySpec(INTERNAL_KEY, "ChaCha20-Poly1305");

    private ChaCha20Algorithm() {}

    private static byte[] generateKey() {
        byte[] key = new byte[KEY_LENGTH];
        RANDOM.nextBytes(key);
        return key;
    }

    @Override
    public byte[] encrypt(byte[] data) throws Exception {
        byte[] nonce = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        Cipher cipher = ENCRYPT_CIPHER.get();
        cipher.init(Cipher.ENCRYPT_MODE, KEY_SPEC, new IvParameterSpec(nonce));
        byte[] encrypted = cipher.doFinal(data);
        ByteBuffer buffer = ByteBuffer.allocate(NONCE_LENGTH + encrypted.length);
        buffer.put(nonce);
        buffer.put(encrypted);
        return buffer.array();
    }

    @Override
    public byte[] decrypt(byte[] data) throws Exception {
        if (data.length <= NONCE_LENGTH) {
            return data; // 太小了不加密
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] nonce = new byte[NONCE_LENGTH];
        buffer.get(nonce);
        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);
        Cipher cipher = DECRYPT_CIPHER.get();
        cipher.init(Cipher.DECRYPT_MODE, KEY_SPEC, new IvParameterSpec(nonce));
        return cipher.doFinal(cipherBytes);
    }

    @Override
    public void setKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashed = sha256.digest(key.getBytes(StandardCharsets.UTF_8));
            KEY_SPEC = new SecretKeySpec(hashed, "ChaCha20-Poly1305");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set key", e);
        }
    }

}
