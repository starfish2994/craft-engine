package net.momirealms.craftengine.bukkit.plugin.agent;

import cn.gtemc.reflection.ImplLookupGetter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.jar.asm.Opcodes;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.Bukkit;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Modifier;

public final class RuntimePatcher {
    private RuntimePatcher() {}

    public static void patch(BukkitCraftEngine plugin) throws Exception {
        Class<?> holderClass = new ByteBuddy()
                .subclass(Object.class)
                .name("net.momirealms.craftengine.bukkit.plugin.agent.PluginHolder")
                .defineField("run", Runnable.class, Modifier.PUBLIC | Modifier.STATIC)
                .make()
                .load(Bukkit.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        holderClass.getDeclaredField("run").set(null, (Runnable) () -> {
            try {
                plugin.injectRegistries();
            } catch (Throwable t) {
                plugin.logger().warn("Failed to inject registries", t);
            }
        });

        byte[] classBytes = getClassBytes("net.minecraft.server.Bootstrap", "net.minecraft.server.DispenserRegistry");
        Class<?> clazz = getClass("net.minecraft.server.Bootstrap", "net.minecraft.server.DispenserRegistry");
        ImplLookupGetter.INSTRUMENTATION.redefineClasses(new ClassDefinition(clazz, transform(classBytes)));
    }

    private static byte[] getClassBytes(String... classNames) {
        for (String className : classNames) {
            className = className.replace('.', '/') + ".class";
            try (InputStream is = Bukkit.class.getClassLoader().getResourceAsStream(className)) {
                if (is == null) continue;
                return is.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Class not found");
    }

    private static Class<?> getClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className, false, Bukkit.class.getClassLoader());
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new RuntimeException("Class not found");
    }

    private static byte[] transform(byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String descriptor, String signature,
                                             String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"()V".equals(descriptor) || !("validate".equals(name) || "c".equals(name))) return mv;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitInsn(int opcode) {
                        if (opcode == Opcodes.RETURN) {
                            mv.visitFieldInsn(Opcodes.GETSTATIC,
                                    "net/momirealms/craftengine/bukkit/plugin/agent/PluginHolder",
                                    "run",
                                    "Ljava/lang/Runnable;"
                            );
                            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                                    "java/lang/Runnable",
                                    "run",
                                    "()V",
                                    true
                            );
                        }
                        super.visitInsn(opcode);
                    }
                };
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
