package net.momirealms.craftengine.core.plugin.script.annotation;

import java.util.List;

public record AnnotatedFunction(String function, List<ScriptAnnotation> annotations) {}
