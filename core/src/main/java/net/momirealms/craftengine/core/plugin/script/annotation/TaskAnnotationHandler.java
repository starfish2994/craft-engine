package net.momirealms.craftengine.core.plugin.script.annotation;

import net.momirealms.craftengine.core.plugin.script.ScriptFile;
import net.momirealms.craftengine.core.plugin.script.ScriptManagerImpl;

public final class TaskAnnotationHandler implements ScriptAnnotationHandler {
    private final ScriptManagerImpl manager;

    public TaskAnnotationHandler(ScriptManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public String name() {
        return "Task";
    }

    @Override
    public void handle(ScriptFile script, String function, ScriptAnnotation annotation) {
        Object period = annotation.option("period");
        if (!(period instanceof Number periodTicks) || periodTicks.longValue() <= 0) {
            this.manager.plugin().logger().warn("Script '" + script.id() + "' has a //@Task annotation without valid 'period' on function '" + function + "'");
            return;
        }
        long delay = annotation.option("delay") instanceof Number d ? d.longValue() : 0;
        boolean async = annotation.boolOption("async", false);
        script.registerTask(function, delay, periodTicks.longValue(), async);
    }
}
