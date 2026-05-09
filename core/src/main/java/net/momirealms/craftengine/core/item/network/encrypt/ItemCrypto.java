package net.momirealms.craftengine.core.item.network.encrypt;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import net.momirealms.sparrow.nbt.ByteArrayTag;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import net.momirealms.sparrow.nbt.Tag;

public final class ItemCrypto {
    private static Algorithm algorithm = NoAlgorithm.INSTANCE;

    static {
        register(Key.ce("none"), NoAlgorithm.INSTANCE);
        register(Key.ce("xor"), XORAlgorithm.INSTANCE);
        register(Key.ce("chacha20"), ChaCha20Algorithm.INSTANCE);
    }

    private ItemCrypto() {}

    public static <T extends Algorithm> ItemCryptoAlgorithm<T> register(Key key, T algorithm) {
        ItemCryptoAlgorithm<T> type = new ItemCryptoAlgorithm<>(key, algorithm);
        ((WritableRegistry<ItemCryptoAlgorithm<? extends Algorithm>>) BuiltInRegistries.ITEM_CRYPTO_ALGORITHM)
                .register(ResourceKey.create(Registries.ITEM_CRYPTO_ALGORITHM.location(), key), type);
        return type;
    }

    public static Tag encrypt(CompoundTag compoundTag) {
        if (compoundTag == null) return null;
        if (!Config.enableItemCrypto()) return compoundTag;
        try {
            return new ByteArrayTag(algorithm.encrypt(NBT.toBytes(compoundTag)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encrypt NBT tag", e);
        }
    }

    public static CompoundTag decrypt(Tag tag) {
        if (tag == null) return null;
        if (!Config.enableItemCrypto()) return (CompoundTag) tag;
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        if (!(tag instanceof ByteArrayTag byteArrayTag)) {
            throw new IllegalArgumentException("Invalid NBT tag");
        }
        try {
            return NBT.fromBytes(algorithm.decrypt(byteArrayTag.getAsByteArray()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt NBT tag", e);
        }
    }

    public static void setKey(String key) {
        algorithm.setKey(key);
    }

    public static void setAlgorithm(Key id) {
        ItemCryptoAlgorithm<? extends Algorithm> value = BuiltInRegistries.ITEM_CRYPTO_ALGORITHM.getValue(id);
        if (value == null) {
            algorithm = NoAlgorithm.INSTANCE;
            CraftEngine.instance().logger().warn("Unknown item crypto algorithm: " + id);
        } else {
            algorithm = value.encryption();
        }
    }
}
