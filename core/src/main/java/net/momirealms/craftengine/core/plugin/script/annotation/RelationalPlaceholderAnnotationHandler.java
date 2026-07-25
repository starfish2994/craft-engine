package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;

/**
 * 处理 //@RelationalPlaceholder("id") 注解：把标记的函数注册为关系占位符（%rel_cejs_id%）。
 * 函数签名：(viewer, target, args) => string，viewer/target 为 CE 玩家包装（可能为 null）。
 */
public final class RelationalPlaceholderAnnotationHandler implements ScriptAnnotationHandler {
    private final ScriptManagerImpl manager;

    public RelationalPlaceholderAnnotationHandler(ScriptManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public String name() {
        return "RelationalPlaceholder";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        String placeholder = annotation.positional(0);
        if (placeholder == null || placeholder.isBlank()) {
            this.manager.plugin().logger().warn("Script '" + script.id() + "' has a //@RelationalPlaceholder annotation without placeholder id on function '" + function + "'");
            return;
        }
        this.manager.registerPlaceholder(script, placeholder, function, true);
    }
}
