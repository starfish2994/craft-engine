package net.momirealms.craftengine.core.plugin.classpath;

import net.momirealms.craftengine.core.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.nio.file.Path;

public final class JdkBuiltinClassPathAppender implements ClassPathAppender {
    private final MethodHandle appendPath;

    private JdkBuiltinClassPathAppender(ClassLoader classLoader, MethodHandle appendPath) {
        this.appendPath = appendPath.bindTo(classLoader);
    }

    public static JdkBuiltinClassPathAppender createIfSupported(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        }

        Method appendMethod = findAppendMethod(classLoader.getClass());
        if (appendMethod == null) {
            return null;
        }

        try {
            return new JdkBuiltinClassPathAppender(
                    classLoader,
                    ReflectionUtils.LOOKUP.unreflect(appendMethod)
            );
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    "Unable to access JDK BuiltinClassLoader classpath appender: "
                            + classLoader.getClass().getName(),
                    e
            );
        }
    }

    private static Method findAppendMethod(Class<?> clazz) {
        Class<?> current = clazz;

        while (current != null) {
            try {
                return current.getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        current = clazz;
        while (current != null) {
            if ("jdk.internal.loader.BuiltinClassLoader".equals(current.getName())) {
                try {
                    return current.getDeclaredMethod("appendClassPath", String.class);
                } catch (NoSuchMethodException ignored) {
                    return null;
                }
            }
            current = current.getSuperclass();
        }

        return null;
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            this.appendPath.invoke(file.toAbsolutePath().normalize().toString());
        } catch (Throwable e) {
            throw new UnsupportedOperationException(
                    "Unable to append jar to JDK BuiltinClassLoader: " + file.toAbsolutePath(),
                    e
            );
        }
    }
}