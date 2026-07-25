package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;

public final class SubscribeAnnotationHandler implements ScriptAnnotationHandler {
    private final ScriptManagerImpl manager;

    public SubscribeAnnotationHandler(ScriptManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public String name() {
        return "Subscribe";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        String eventClass = annotation.positional(0);
        if (eventClass == null) {
            this.manager.plugin().logger().warn("Script '" + script.id() + "' has a //@Subscribe annotation without event class on function '" + function + "'");
            return;
        }
        this.manager.subscribeEvent(script, eventClass, function, annotation.named());
    }
}
