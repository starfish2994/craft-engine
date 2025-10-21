package net.momirealms.craftengine.core.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.momirealms.craftengine.core.pack.ResourceLocation;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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

    public static void copyFilesByExtension(Path sourceDir, Path targetDir, String fileExtension, boolean preserveStructure) throws IOException {
        if (!Files.exists(sourceDir)) {
            return;
        }

        if (!Files.isDirectory(sourceDir)) {
            return;
        }

        // 确保目标目录存在
        Files.createDirectories(targetDir);
        String extensionPattern = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(extensionPattern.toLowerCase()))
                    .forEach(sourceFile -> {
                        try {
                            Path targetFile;
                            if (preserveStructure) {
                                // 保持目录结构
                                targetFile = targetDir.resolve(sourceDir.relativize(sourceFile));
                            } else {
                                // 不保持目录结构，所有文件都放在目标目录根下
                                targetFile = targetDir.resolve(sourceFile.getFileName());
                            }
                            // 确保目标文件的父目录存在
                            Files.createDirectories(targetFile.getParent());
                            // 复制文件，如果已存在则替换
                            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to copy file: " + sourceFile, e);
                        }
                    });
        }
    }
}
