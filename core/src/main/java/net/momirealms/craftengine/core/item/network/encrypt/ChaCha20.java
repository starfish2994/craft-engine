package net.momirealms.craftengine.core.item.network.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class ChaCha20 implements CryptoAlgorithm {
    private static final int NONCE_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();
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
    private final SecretKeySpec keySpec;

    public ChaCha20(String key) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashed = sha256.digest(key.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(hashed, "ChaCha20-Poly1305");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set key", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data) throws Exception {
        byte[] nonce = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        Cipher cipher = ENCRYPT_CIPHER.get();
        cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, new IvParameterSpec(nonce));
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
        cipher.init(Cipher.DECRYPT_MODE, this.keySpec, new IvParameterSpec(nonce));
        return cipher.doFinal(cipherBytes);
    }
}
