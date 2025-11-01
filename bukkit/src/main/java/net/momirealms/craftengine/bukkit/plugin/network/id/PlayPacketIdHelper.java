package net.momirealms.craftengine.bukkit.plugin.network.id;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PlayPacketIdHelper {
    // 1.20.5-latest
    private static final Map<PacketFlow, Map<String, Integer>> byName = new EnumMap<>(PacketFlow.class);
    private static final Map<PacketFlow, String[]> byId = new EnumMap<>(PacketFlow.class);
    // 1.20-1.20.4
    private static final Map<PacketFlow, Map<Class<?>, Integer>> byClazz = new EnumMap<>(PacketFlow.class);

    static {
        try {
            if (VersionHelper.isOrAbove1_20_5()) {
                byName.putAll(FastNMS.INSTANCE.gamePacketIdsByName().get(ConnectionState.PLAY));
            } else {
                byClazz.putAll(FastNMS.INSTANCE.gamePacketIdsByClazz().get(ConnectionState.PLAY));
            }
            if (!byName.isEmpty()) {
                for (Map.Entry<PacketFlow, Map<String, Integer>> entry : byName.entrySet()) {
                    String[] ids = new String[entry.getValue().size()];
                    for (Map.Entry<String, Integer> nameIdEntry : entry.getValue().entrySet()) {
                        ids[nameIdEntry.getValue()] = nameIdEntry.getKey();
                    }
                    byId.put(entry.getKey(), ids);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to init packet registry", e);
        }
    }

    public static int count(PacketFlow direction) {
        if (VersionHelper.isOrAbove1_20_5()) {
            return byName.getOrDefault(direction, Collections.emptyMap()).size();
        } else {
            return byClazz.getOrDefault(direction, Collections.emptyMap()).size();
        }
    }

    public static String byId(int id, PacketFlow direction) {
        return byId.get(direction)[id];
    }

    public static int byName(String packetName, PacketFlow direction) {
        return byName.get(direction).getOrDefault(packetName, -1);
    }

    public static int byClazz(Class<?> clazz, PacketFlow direction) {
        return byClazz.get(direction).getOrDefault(clazz, -1);
    }
}
