package net.momirealms.craftengine.bukkit.plugin.agent;

import cn.gtemc.reflection.ImplLookupGetter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;

import java.lang.instrument.Instrumentation;
import java.util.function.Consumer;

public final class RuntimePatcher {
    private static Instrumentation instrumentation;
    private static Class<?> injectedBridge;

    private RuntimePatcher() {}

    public static void patch(BukkitCraftEngine plugin) throws Exception {
        boolean registryInjection = !isDatapackDiscoveryAvailable();
        boolean chunkDataWarmup = VersionHelper.hasPaperPatch && VersionHelper.isOrAbove1_21_4;
        if (!registryInjection && !chunkDataWarmup) return;

        if (registryInjection) {
            Class<?> bridge = injectBridge();
            Instrumentation inst = instrumentation();
            bridge.getField("REGISTRY_INJECTION").set(null, (Runnable) () -> {
                try {
                    plugin.injectRegistries();
                    inst.removeTransformer(BlocksAgent.transformer);
                } catch (Throwable t) {
                    plugin.logger().warn("Failed to inject registries", t);
                }
            });
            BlocksAgent.install(inst);
        }

        if (chunkDataWarmup) {
            try {
                Class<?> bridge = injectBridge();
                bridge.getField("CHUNK_DATA_WARMUP").set(null, (Consumer<Object[]>) BukkitWorldManager::onChunkDataRead);
                ChunkLoadWarmupAgent.install(instrumentation());
                plugin.logger().info("Hooked into chunk data read for chunk data warmup");
            } catch (Throwable t) {
                plugin.logger().warn("Failed to hook chunk data read, chunk data will be read synchronously on chunk load", t);
            }
        }
    }

    private static Class<?> injectBridge() {
        if (injectedBridge == null) {
            ClassLoader serverClassLoader = Bukkit.class.getClassLoader();
            new ByteBuddy()
                    .redefine(AgentBridge.class)
                    .make()
                    .load(serverClassLoader, ClassLoadingStrategy.Default.INJECTION);
            try {
                injectedBridge = Class.forName(AgentBridge.class.getName(), false, serverClassLoader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to inject agent bridge", e);
            }
        }
        return injectedBridge;
    }

    private static Instrumentation instrumentation() {
        if (instrumentation == null) {
            instrumentation = ReflectionUtils.JNI_IS_AVAILABLE ? ImplLookupGetter.INSTRUMENTATION : ByteBuddyAgent.install();
        }
        return instrumentation;
    }

    public static boolean isDatapackDiscoveryAvailable() {
        try {
            Class<?> eventsClass = Class.forName("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents");
            eventsClass.getField("DATAPACK_DISCOVERY");
            return true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            return false;
        }
    }
}
