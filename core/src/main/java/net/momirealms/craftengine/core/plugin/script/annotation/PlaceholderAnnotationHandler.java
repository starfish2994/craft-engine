package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;

public final class PlaceholderAnnotationHandler implements ScriptAnnotationHandler {
    private final ScriptManagerImpl manager;

    public PlaceholderAnnotationHandler(ScriptManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public String name() {
        return "Placeholder";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        String placeholder = annotation.positional(0);
        if (placeholder == null || placeholder.isBlank()) {
            this.manager.plugin().logger().warn("Script '" + script.id() + "' has a //@Placeholder annotation without placeholder id on function '" + function + "'");
            return;
        }
        this.manager.registerPlaceholder(script, placeholder, function, false);
    }
}
