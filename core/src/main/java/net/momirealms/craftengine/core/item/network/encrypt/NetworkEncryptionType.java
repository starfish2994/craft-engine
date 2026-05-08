package net.momirealms.craftengine.core.item.network.encrypt;

import net.momirealms.craftengine.core.util.Key;

public record NetworkEncryptionType<T extends Encryption>(Key id, T encryption) {
}
