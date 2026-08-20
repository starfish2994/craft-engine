package net.momirealms.craftengine.core.plugin.dependency;

import net.momirealms.craftengine.core.plugin.PluginProperties;
import net.momirealms.craftengine.core.plugin.dependency.relocation.Relocation;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Dependency {
    private final String id;
    private final String versionKey;
    private final String groupId;
    private final String rawArtifactId;
    private final List<Relocation> relocations;
    private final DependencyVisibility visibility;
    private final String jarInJarPath;

    private Dependency(Builder builder) {
        this.id = builder.id;
        this.versionKey = builder.versionKey != null ? builder.versionKey : builder.id;
        this.groupId = builder.groupId;
        this.rawArtifactId = builder.artifactId;
        this.relocations = builder.relocations;
        this.visibility = builder.visibility;
        this.jarInJarPath = builder.jarInJarPath;
    }

    public static Builder of(String id, String groupId, String artifactId) {
        return new Builder(id, groupId, artifactId);
    }

    public static final class Builder {
        private final String id;
        private final String groupId;
        private final String artifactId;
        private String versionKey;
        private List<Relocation> relocations = Collections.emptyList();
        private DependencyVisibility visibility = DependencyVisibility.INTERNAL;
        private String jarInJarPath;

        private Builder(String id, String groupId, String artifactId) {
            this.id = id;
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public Builder versionKey(String versionKey) {
            this.versionKey = versionKey;
            return this;
        }

        public Builder relocations(Relocation... relocations) {
            this.relocations = List.of(relocations);
            return this;
        }

        public Builder relocations(List<Relocation> relocations) {
            this.relocations = relocations;
            return this;
        }

        public Builder visibility(DependencyVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder jarInJarPath(String jarInJarPath) {
            this.jarInJarPath = jarInJarPath;
            return this;
        }

        public Dependency build() {
            return new Dependency(this);
        }
    }

    public DependencyVisibility visibility() {
        return this.visibility;
    }

    public String id() {
        return this.id;
    }

    public String versionKey() {
        return this.versionKey;
    }

    public String groupId() {
        return this.groupId;
    }

    public String artifactId() {
        return this.rawArtifactId;
    }

    public String classifier() {
        return "";
    }

    public List<Relocation> relocations() {
        return this.relocations;
    }

    public String toLocalPath() {
        return rewriteEscaping(this.groupId).replace(".", "/") + "/" + this.rawArtifactId + "/" + getVersion();
    }

    public boolean hasJarInJarPath() {
        return this.jarInJarPath != null;
    }

    public String jarInJarPath() {
        return jarInJarPath;
    }

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s.jar";

    public String mavenPath() {
        return String.format(MAVEN_FORMAT,
                rewriteEscaping(this.groupId()).replace(".", "/"),
                rewriteEscaping(this.artifactId()),
                getVersion(),
                rewriteEscaping(this.artifactId()) + "-" + getVersion() + (classifier().isEmpty() ? "" : "-" + classifier())
        );
    }

    public String fileName(String classifier) {
        String name = this.artifactId().toLowerCase(Locale.ENGLISH).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty()
                ? ""
                : "-" + classifier;
        return name + "-" + this.getVersion() + extra + ".jar";
    }

    public String getVersion() {
        return PluginProperties.getValue(this.versionKey);
    }

    public static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dependency that)) return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
