package net.momirealms.craftengine.core.item.network.encrypt;

public interface Algorithm {

    byte[] encrypt(byte[] data) throws Exception;

    byte[] decrypt(byte[] data) throws Exception;

    void setKey(String key);

}
