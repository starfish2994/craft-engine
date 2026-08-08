package net.momirealms.craftengine.bukkit.plugin.agent;

import java.util.function.Consumer;

public final class AgentBridge {
    /** 注册表注入回调，由织入 Bootstrap#validate/DispenserRegistry 的 advice 触发（旧版本服务端） */
    public static Runnable REGISTRY_INJECTION;
    /** 区块数据预热回调（{world, chunkPos, protoChunk}），由织入 SerializableChunkData#read 的 advice 触发 */
    public static volatile Consumer<Object[]> CHUNK_DATA_WARMUP;

    private AgentBridge() {}
}
