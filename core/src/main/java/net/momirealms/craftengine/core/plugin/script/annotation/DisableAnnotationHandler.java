package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;

import java.util.Map;

public final class DisableAnnotationHandler implements ScriptAnnotationHandler {

    @Override
    public String name() {
        return "Disable";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        script.onUnload(() -> script.invoke(function, Map.of()));
    }
}
