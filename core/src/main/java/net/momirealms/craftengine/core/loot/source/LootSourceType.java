package net.momirealms.craftengine.core.loot.source;

import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.LootManager;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootSourceType<T extends LootSource> {
    private final Key id;
    private final LootSourceFactory<T> factory;
    private volatile SourceIndex sources = SourceIndex.EMPTY;

    public LootSourceType(Key id, LootSourceFactory<T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public Key id() {
        return this.id;
    }

    public LootSourceFactory<T> factory() {
        return this.factory;
    }

    public boolean hasSources() {
        return this.sources != SourceIndex.EMPTY;
    }

    public List<LootSource> getSources(@Nullable Key target) {
        SourceIndex index = this.sources;
        if (target == null) return index.wildcard();
        List<LootSource> targeted = index.targeted().get(target);
        if (targeted == null) return index.wildcard();
        if (index.wildcard().isEmpty()) return targeted;
        List<LootSource> merged = new ArrayList<>(index.wildcard().size() + targeted.size());
        merged.addAll(index.wildcard());
        merged.addAll(targeted);
        return merged;
    }

    @ApiStatus.Internal
    public void updateSources(List<LootSource> sources) {
        this.sources = sources.isEmpty() ? SourceIndex.EMPTY : SourceIndex.of(sources);
    }

    @ApiStatus.Internal
    public void clearSources() {
        this.sources = SourceIndex.EMPTY;
    }

    public LootOutcome eval(@Nullable Key target, LootContext context) {
        return LootManager.eval(getSources(target), context);
    }

    private record SourceIndex(List<LootSource> wildcard, Map<Key, List<LootSource>> targeted) {
        private static final SourceIndex EMPTY = new SourceIndex(List.of(), Map.of());

        private static SourceIndex of(List<LootSource> sources) {
            List<LootSource> wildcard = new ArrayList<>();
            Map<Key, List<LootSource>> targeted = new HashMap<>();
            for (LootSource source : sources) {
                if (source.targets().isEmpty()) {
                    wildcard.add(source);
                } else {
                    for (Key targetKey : source.targets()) {
                        targeted.computeIfAbsent(targetKey, k -> new ArrayList<>()).add(source);
                    }
                }
            }
            Map<Key, List<LootSource>> frozenTargeted = new HashMap<>();
            targeted.forEach((key, list) -> frozenTargeted.put(key, List.copyOf(list)));
            return new SourceIndex(List.copyOf(wildcard), Map.copyOf(frozenTargeted));
        }
    }
}
