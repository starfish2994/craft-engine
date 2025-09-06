package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.core.pack.PackCacheData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncResourcePackCacheEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final PackCacheData cacheData;

    public AsyncResourcePackCacheEvent(@NotNull PackCacheData cacheData) {
        super(true);
        this.cacheData = cacheData;
    }

    @NotNull
    public PackCacheData cacheData() {
        return cacheData;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
