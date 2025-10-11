package net.momirealms.craftengine.core.pack.model.generation;

import java.util.Collection;

public interface ModelGenerator {

    Collection<ModelGeneration> modelsToGenerate();

    void clearModelsToGenerate();
}
