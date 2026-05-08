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

public final class NetworkEncryption {
    private static Encryption encryption = NoEncryption.INSTANCE;

    static {
        register(Key.ce("none"),NoEncryption.INSTANCE);
        register(Key.ce("xor"),XOREncryption.INSTANCE);
        register(Key.ce("chacha20"),ChaCha20Encryption.INSTANCE);
    }

    private NetworkEncryption() {}

    public static <T extends Encryption> NetworkEncryptionType<T> register(Key key, T encryption) {
        NetworkEncryptionType<T> type = new NetworkEncryptionType<>(key, encryption);
        ((WritableRegistry<NetworkEncryptionType<? extends Encryption>>) BuiltInRegistries.NETWORK_ENCRYPTION_TYPE)
                .register(ResourceKey.create(Registries.NETWORK_ENCRYPTION_TYPE.location(), key), type);
        return type;
    }

    public static Tag encrypt(CompoundTag compoundTag) {
        if (compoundTag == null) return null;
        if (!Config.enableNetworkDataProtection()) return compoundTag;
        try {
            return new ByteArrayTag(encryption.encrypt(NBT.toBytes(compoundTag)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encrypt NBT tag", e);
        }
    }

    public static CompoundTag decrypt(Tag tag) {
        if (tag == null) return null;
        if (!Config.enableNetworkDataProtection()) return (CompoundTag) tag;
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        if (!(tag instanceof ByteArrayTag byteArrayTag)) {
            throw new IllegalArgumentException("Invalid NBT tag");
        }
        try {
            return NBT.fromBytes(encryption.decrypt(byteArrayTag.getAsByteArray()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt NBT tag", e);
        }
    }

    public static void setKey(String key) {
        encryption.setKey(key);
    }

    public static void setEncryption(Key id) {
        NetworkEncryptionType<? extends Encryption> value = BuiltInRegistries.NETWORK_ENCRYPTION_TYPE.getValue(id);
        if (value == null) {
            encryption = NoEncryption.INSTANCE;
            CraftEngine.instance().logger().warn("Unknown network encryption type: " + id);
        } else {
            encryption = value.encryption();
        }
    }
}
