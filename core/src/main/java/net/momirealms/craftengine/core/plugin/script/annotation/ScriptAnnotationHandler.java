package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;

public interface ScriptAnnotationHandler {

    String name();

    void handle(ScriptFile script, String function, ScriptAnnotation annotation);
}
