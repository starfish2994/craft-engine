package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.SimpleContext;
import net.momirealms.craftengine.core.util.MiscUtils;

public interface NumberProvider {

    float getFloat(Context context);

    double getDouble(Context context);

    default int getInt(Context context) {
        return MiscUtils.floor(this.getDouble(context));
    }

    default float getFloat() {
        return getFloat(SimpleContext.EMPTY);
    }

    default double getDouble() {
        return getDouble(SimpleContext.EMPTY);
    }

    default int getInt() {
        return getInt(SimpleContext.EMPTY);
    }

    default boolean isConstant() {
        return false;
    }
}
