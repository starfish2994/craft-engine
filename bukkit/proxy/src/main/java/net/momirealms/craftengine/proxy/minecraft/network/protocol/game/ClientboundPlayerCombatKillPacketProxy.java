package net.momirealms.craftengine.proxy.minecraft.network.protocol.game;

import net.momirealms.craftengine.proxy.minecraft.network.protocol.PacketProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket")
public interface ClientboundPlayerCombatKillPacketProxy extends PacketProxy {
    ClientboundPlayerCombatKillPacketProxy INSTANCE = ASMProxyFactory.create(ClientboundPlayerCombatKillPacketProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket");
}
