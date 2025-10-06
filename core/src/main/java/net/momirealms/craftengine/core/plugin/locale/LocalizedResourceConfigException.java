package net.momirealms.craftengine.core.plugin.locale;

import net.momirealms.craftengine.core.util.ArrayUtils;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class LocalizedResourceConfigException extends LocalizedException {

    public LocalizedResourceConfigException(
            @NotNull String node,
            @Nullable Exception cause,
            @Nullable String... arguments
    ) {
        super(node, cause, ArrayUtils.merge(new String[] {"null", "null"}, arguments));
    }

    public LocalizedResourceConfigException(
            @NotNull String node,
            @Nullable String... arguments
    ) {
        super(node, (Exception) null, ArrayUtils.merge(new String[] {"null", "null"}, arguments));
    }

    public LocalizedResourceConfigException(
            @NotNull String node,
            @NotNull Path path,
            @NotNull Key id,
            @Nullable String... arguments
    ) {
        super(node, (Exception) null, ArrayUtils.merge(new String[] {path.toString(), id.toString()}, arguments));
    }

    public void setPath(Path path) {
        super.setArgument(0, path.toString());
    }

    public void setNode(String id) {
        super.setArgument(1, id);
    }
}
