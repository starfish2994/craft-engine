package net.momirealms.craftengine.bukkit.plugin.classpath;

import net.momirealms.craftengine.core.plugin.classpath.ClassPathAppender;
import net.momirealms.craftengine.core.plugin.classpath.JdkBuiltinClassPathAppender;
import net.momirealms.craftengine.core.plugin.classpath.URLClassLoaderAccess;
import org.bukkit.Bukkit;

import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class BukkitClassPathAppender implements ClassPathAppender {
    private final ClassPathAppender delegate;

    public BukkitClassPathAppender() {
        ClassLoader bukkitClassLoader = Bukkit.class.getClassLoader();

        // production env, server launched with a real jar file
        URLClassLoader urlClassLoader = findURLClassLoader(bukkitClassLoader);
        if (urlClassLoader != null) {
            URLClassLoaderAccess access = URLClassLoaderAccess.create(urlClassLoader);
            this.delegate = file -> {
                try {
                    access.addURL(file.toUri().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            };
            return;
        }

        // fallback, we are in the development environment
        JdkBuiltinClassPathAppender jdkBuiltinAppender =
                JdkBuiltinClassPathAppender.createIfSupported(bukkitClassLoader);

        if (jdkBuiltinAppender != null) {
            this.delegate = jdkBuiltinAppender;
            return;
        }

        throw new UnsupportedOperationException(
                "Unsupported classloader " + bukkitClassLoader.getClass()
                        + ". Expected URLClassLoader or JDK BuiltinClassLoader/AppClassLoader."
        );
    }

    private static URLClassLoader findURLClassLoader(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            return urlClassLoader;
        }

        ClassLoader parent = classLoader.getParent();
        return parent == null ? null : findURLClassLoader(parent);
    }

    @Override
    public void addJarToClasspath(Path file) {
        this.delegate.addJarToClasspath(file);
    }
}