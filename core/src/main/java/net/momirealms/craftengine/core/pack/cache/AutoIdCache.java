package net.momirealms.craftengine.core.pack.cache;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class AutoIdCache {
    private final Map<String, Integer> forcedIds = new HashMap<>();
    private final Map<String, Integer> cachedIds = new HashMap<>();
    private final BitSet occupiedIds = new BitSet();
    private int currentAutoId;

    public AutoIdCache(int startIndex) {
        this.currentAutoId = startIndex;
    }

    public boolean setForcedId(final String name, int index) {
        if (this.occupiedIds.get(index)) {
            return false;
        }
        this.occupiedIds.set(index);
        this.forcedIds.put(name, index);
        return true;
    }

    
}
