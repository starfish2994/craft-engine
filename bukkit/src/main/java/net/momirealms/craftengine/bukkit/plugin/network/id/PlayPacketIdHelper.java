package net.momirealms.craftengine.bukkit.plugin.network.id;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.*;

public class PlayPacketIdHelper {
    // 1.20.5-latest
    private static final Map<PacketFlow, Map<String, Integer>> byName = new EnumMap<>(PacketFlow.class);
    private static final Map<PacketFlow, String[]> byId = new EnumMap<>(PacketFlow.class);
    // 1.20-1.20.4
    private static final Map<PacketFlow, Map<Class<?>, Integer>> byClazz = new EnumMap<>(PacketFlow.class);

    static {
        try {
            if (VersionHelper.isOrAbove1_21()) {
                Object packetReport = CoreReflections.constructor$PacketReport.newInstance((Object) null);
                JsonObject packetReportData = ((JsonElement) CoreReflections.method$PacketReport$serializePackets.invoke(packetReport)).getAsJsonObject();
                JsonObject playData = packetReportData.get("play").getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : playData.entrySet()) {
                    Map<String, Integer> ids = new HashMap<>();
                    byName.put(PacketFlow.valueOf(entry.getKey().toUpperCase(Locale.ROOT)), ids);
                    for (var entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                        ids.put(entry2.getKey(), entry2.getValue().getAsJsonObject().get("protocol_id").getAsInt());
                    }
                }
            } else if (VersionHelper.isOrAbove1_20_5()) {
                for (Map.Entry<String, Map<String, Integer>> entry : FastNMS.INSTANCE.gamePacketIdsByName().entrySet()) {
                    byName.put(PacketFlow.valueOf(entry.getKey().toUpperCase(Locale.ROOT)), entry.getValue());
                }
            } else {
                for (Map.Entry<String, Map<Class<?>, Integer>> entry : FastNMS.INSTANCE.gamePacketIdsByClazz().entrySet()) {
                    byClazz.put(PacketFlow.valueOf(entry.getKey().toUpperCase(Locale.ROOT)), entry.getValue());
                }
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
