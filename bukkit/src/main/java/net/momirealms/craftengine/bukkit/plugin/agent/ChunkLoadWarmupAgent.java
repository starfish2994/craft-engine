package net.momirealms.craftengine.bukkit.plugin.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.function.Consumer;

public final class ChunkLoadWarmupAgent {
    public static ClassFileTransformer transformer;

    private ChunkLoadWarmupAgent() {}

    public static void install(Instrumentation instrumentation) {
        transformer = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(ElementMatchers.named("net.minecraft.world.level.chunk.storage.SerializableChunkData"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(WarmupAdvice.class)
                                .on(ElementMatchers.named("read").and(ElementMatchers.takesArguments(4)))))
                .installOn(instrumentation);
    }

    public static class WarmupAdvice {

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) Object world,
                                  @Advice.Argument(3) Object chunkPos,
                                  @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object chunk) {
            try {
                if (chunk == null) return;
                // AgentBridge 已被注入服务端类加载器，此处解析的是注入的副本
                Consumer<Object[]> callback = AgentBridge.CHUNK_DATA_WARMUP;
                if (callback != null) {
                    callback.accept(new Object[]{world, chunkPos, chunk});
                }
            } catch (Throwable ignored) {
                // 预热失败绝不能影响原版区块加载
            }
        }
    }
}
