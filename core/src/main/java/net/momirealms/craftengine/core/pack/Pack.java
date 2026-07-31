package net.momirealms.craftengine.core.pack;

import java.nio.file.Path;

/**
 * Represents a folder under the user's resources directory,
 * designed to simplify the installation of third-party resource packs.
 * <p>
 * The folder structure allows users to organize and manage
 * resource packs and configurations provided by external sources.
 * <p>
 * This class provides access to the resource pack folder
 * and configuration folder within the specified directory.
 */
public final class Pack {
    private final Path folder;
    private final PackMeta meta;
    private final boolean enabled;
    private final String[] subpacks;
    private final Path[] resourcePackFolders;
    private final Path[] configurationFolders;
    private final Path[] blueprintFolders;
    private final Path[] scriptFolders;

    public Pack(Path folder, PackMeta meta, boolean enabled, String[] subpacks) {
        this.folder = folder;
        this.meta = meta;
        this.enabled = enabled;
        this.subpacks = subpacks;
        this.resourcePackFolders = expandFolders("resourcepack");
        this.configurationFolders = expandFolders("configuration");
        this.blueprintFolders = expandFolders("blueprint");
        this.scriptFolders = expandFolders("script");
    }

    public String name() {
        return this.folder.getFileName().toString();
    }

    public String namespace() {
        return this.meta.namespace();
    }

    public boolean enabled() {
        return this.enabled;
    }

    public PackMeta meta() {
        return this.meta;
    }

    public Path folder() {
        return this.folder;
    }

    /**
     * Returns the 'resourcepack' folder within the specified directory,
     * used for storing third-party resource packs.
     */
    public Path resourcePackFolder() {
        return this.resourcePackFolders[0];
    }

    /**
     * Returns the 'configuration' folder within the specified directory,
     * used for storing configuration files related to the resource packs.
     */
    public Path configurationFolder() {
        return this.configurationFolders[0];
    }

    /**
     * Returns the 'blueprint' folder within the specified directory,
     * used for storing Blockbench .bbmodel files referenced by 'blueprint' options.
     */
    public Path blueprintFolder() {
        return this.blueprintFolders[0];
    }

    /**
     * Returns the 'script' folder within the specified directory,
     * used for storing js scripts.
     */
    public Path scriptFolder() {
        return this.scriptFolders[0];
    }

    public Path[] resourcePackFolders() {
        return this.resourcePackFolders;
    }

    public Path[] configurationFolders() {
        return this.configurationFolders;
    }

    public Path[] blueprintFolders() {
        return this.blueprintFolders;
    }

    public Path[] scriptFolders() {
        return this.scriptFolders;
    }

    /**
     * Returns the named folder of this pack and of every subpack,
     * main pack first.
     */
    private Path[] expandFolders(String name) {
        Path[] folders = new Path[1 + this.subpacks.length];
        folders[0] = this.folder.resolve(name);
        for (int i = 1; i <= this.subpacks.length; i++) {
            folders[i] = this.folder.resolve("subpacks").resolve(this.subpacks[i - 1]).resolve(name);
        }
        return folders;
    }
}
