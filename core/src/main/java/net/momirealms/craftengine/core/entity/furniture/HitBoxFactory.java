package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public interface HitBoxFactory {

    HitBox create(Map<String, Object> arguments);

    @SuppressWarnings("unchecked")
    static Seat[] getSeats(Map<String, Object> arguments) {
        List<String> seats = (List<String>) arguments.getOrDefault("seats", List.of());
        return seats.stream()
                .map(arg -> {
                    String[] split = arg.split(" ");
                    if (split.length == 1) return new Seat(ResourceConfigUtils.getAsVector3f(split[0], "seats"), 0, false);
                    return new Seat(ResourceConfigUtils.getAsVector3f(split[0], "seats"), Float.parseFloat(split[1]), true);
                })
                .toArray(Seat[]::new);
    }
}
