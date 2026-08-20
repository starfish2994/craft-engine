package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;

public final class EnableAnnotationHandler implements ScriptAnnotationHandler {
    private final ScriptManagerImpl manager;

    public EnableAnnotationHandler(ScriptManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public String name() {
        return "Enable";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        this.manager.addPendingEnable(script, function);
    }
}
