package net.momirealms.craftengine.proxy.paper.antixray;

import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "io.papermc.paper.antixray.ChunkPacketInfo", activeIf = "has_patch=paper")
public interface ChunkPacketInfoProxy {
}
