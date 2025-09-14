package net.momirealms.craftengine.bukkit.plugin.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class BlocksAgent {

    public static void agentmain(String args, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(ElementMatchers.named("net.minecraft.server.Bootstrap")
                        .or(ElementMatchers.named("net.minecraft.server.DispenserRegistry")))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(BlocksAdvice.class)
                                .on(ElementMatchers.named("validate")
                                        .or(ElementMatchers.named("c")))))
                .installOn(instrumentation);
    }

    public static class BlocksAdvice {

        @Advice.OnMethodExit
        public static void onExit() {
            try {
                Class<?> holder = Class.forName("net.momirealms.craftengine.bukkit.plugin.agent.PluginHolder");
                Field field = holder.getField("plugin");
                Object plugin = field.get(null);
                Method injectRegistries = plugin.getClass().getMethod("injectRegistries");
                injectRegistries.invoke(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
