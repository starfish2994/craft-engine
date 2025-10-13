package net.momirealms.craftengine.core.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.momirealms.craftengine.core.pack.ResourceLocation;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {}

    public static boolean isAbsolute(final String path) {
        return path.startsWith("/") || path.matches("^[A-Za-z]:\\\\.*");
    }

    public static String getExtension(Path path) {
        final String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return "";
        } else {
            return name.substring(index + 1);
        }
    }

    public static String pathWithoutExtension(String path) {
        int i = path.lastIndexOf('.');
        return i == -1 ? path : path.substring(0, i);
    }

    public static void createDirectoriesSafe(Path path) throws IOException {
        Files.createDirectories(Files.exists(path) ? path.toRealPath() : path);
    }

    public static void deleteDirectory(Path folder) throws IOException {
        if (!Files.exists(folder)) return;
        try (Stream<Path> walk = Files.walk(folder, FileVisitOption.FOLLOW_LINKS)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ioException) {
                            throw new RuntimeException(ioException);
                        }
                    });
        }
    }

    public static List<Path> getYmlConfigsDeeply(Path configFolder) {
        if (!Files.exists(configFolder)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(configFolder, FileVisitOption.FOLLOW_LINKS)) {
            return stream.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse directory: " + configFolder, e);
        }
    }

    public static List<Path> collectOverlays(Path resourcePackFolder) throws IOException {
        List<Path> folders = new ObjectArrayList<>();
        folders.add(resourcePackFolder);
        try (Stream<Path> paths = Files.list(resourcePackFolder)) {
            folders.addAll(paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals("assets"))
                    .filter(path -> Files.exists(path.resolve("assets")))
                    .toList());
        }
        return folders;
    }

    public static List<Path> collectNamespaces(Path assetsFolder) throws IOException {
        List<Path> folders;
        try (Stream<Path> paths = Files.list(assetsFolder)) {
            folders = new ObjectArrayList<>(paths
                    .filter(Files::isDirectory)
                    .filter(path -> ResourceLocation.isValidNamespace(path.getFileName().toString()))
                    .toList());
        }
        return folders;
    }
}
