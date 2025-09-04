package net.momirealms.craftengine.bukkit.plugin.classpath;

import net.momirealms.craftengine.core.plugin.classpath.ClassPathAppender;
import net.momirealms.craftengine.core.plugin.classpath.URLClassLoaderAccess;
import org.bukkit.Bukkit;

import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class BukkitClassPathAppender implements ClassPathAppender {
    private final URLClassLoaderAccess libraryClassLoaderAccess;

    public BukkitClassPathAppender() {
        // 这个类加载器用于加载重定位后的依赖库，这样所有插件都能访问到
        ClassLoader bukkitClassLoader = Bukkit.class.getClassLoader();
        if (bukkitClassLoader instanceof URLClassLoader urlClassLoader) {
            this.libraryClassLoaderAccess = URLClassLoaderAccess.create(urlClassLoader);
        } else {
            // ignite会把Bukkit放置于EmberClassLoader中，获取其父DynamicClassLoader
            if (bukkitClassLoader.getClass().getName().equals("space.vectrix.ignite.launch.ember.EmberClassLoader") && bukkitClassLoader.getParent() instanceof URLClassLoader urlClassLoader) {
                this.libraryClassLoaderAccess = URLClassLoaderAccess.create(urlClassLoader);
            } else {
                throw new UnsupportedOperationException("Unsupported classloader " + bukkitClassLoader.getClass());
            }
        }
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            this.libraryClassLoaderAccess.addURL(file.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
