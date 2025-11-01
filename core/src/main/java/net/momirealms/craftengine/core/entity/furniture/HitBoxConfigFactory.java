package net.momirealms.craftengine.core.entity.furniture;

import java.util.Map;

public interface HitBoxConfigFactory {

    HitBoxConfig create(Map<String, Object> arguments);
}
