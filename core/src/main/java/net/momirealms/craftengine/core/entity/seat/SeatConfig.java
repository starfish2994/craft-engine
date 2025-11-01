package net.momirealms.craftengine.core.entity.seat;

import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.joml.Vector3f;

import java.util.List;

public record SeatConfig(Vector3f position, float yRot, boolean limitPlayerRotation) {

    public static SeatConfig[] fromObj(Object config) {
        if (config instanceof List<?>) {
            List<String> seats = MiscUtils.getAsStringList(config);
            return seats.stream()
                    .map(arg -> {
                        String[] split = arg.split(" ");
                        if (split.length == 1) return new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), 0, false);
                        return new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), Float.parseFloat(split[1]), true);
                    })
                    .toArray(SeatConfig[]::new);
        } else if (config != null) {
            String arg = config.toString();
            String[] split = arg.split(" ");
            if (split.length == 1) return new SeatConfig[] {new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), 0, false)};
            return new SeatConfig[] {new SeatConfig(ResourceConfigUtils.getAsVector3f(split[0], "seats"), Float.parseFloat(split[1]), true)};
        } else {
            return new SeatConfig[0];
        }
    }
}
