package net.momirealms.craftengine.core.item.network.encrypt;

import net.momirealms.craftengine.core.util.Key;

public record ItemCryptoAlgorithm<T extends CryptoAlgorithm>(Key id, T encryption) {
}
