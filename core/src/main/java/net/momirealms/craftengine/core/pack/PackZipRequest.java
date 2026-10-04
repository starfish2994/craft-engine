package net.momirealms.craftengine.core.pack;

import java.nio.file.Path;

public record PackZipRequest(Path source, Path output, boolean protection, boolean storePng) {
    public PackZipRequest(Path source, Path output, boolean protection) {
        this(source, output, protection, false);
    }
}
