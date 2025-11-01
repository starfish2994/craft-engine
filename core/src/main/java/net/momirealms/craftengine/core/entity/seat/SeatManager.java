package net.momirealms.craftengine.core.entity.seat;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.util.Key;

public interface SeatManager extends Manageable {
    Key SEAT_KEY = Key.of("craftengine:seat");
    Key SEAT_EXTRA_DATA_KEY = Key.of("craftengine:seat_extra_data");
}
