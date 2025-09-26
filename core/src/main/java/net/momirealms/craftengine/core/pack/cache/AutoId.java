package net.momirealms.craftengine.core.pack.cache;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.core.util.FileUtils;
import net.momirealms.craftengine.core.util.GsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class AutoId {
    private final Path cachePath;
    private final BiMap<String, Integer> forcedIds = HashBiMap.create(128);

    private final Map<String, Integer> cachedIds = new HashMap<>();
    private final BitSet occupiedIds = new BitSet();
    private final Map<String, CompletableFuture<Integer>> autoIds = new HashMap<>();
    private int currentAutoId;
    private int minId;
    private int maxId;

    public AutoId(Path cachePath) {
        this.cachePath = cachePath;
    }

    public void reset(int startIndex, int endIndex) {
        this.minId = startIndex;
        this.currentAutoId = startIndex;
        this.maxId = endIndex;
        this.occupiedIds.clear();
        this.forcedIds.clear();
        this.autoIds.clear();
        this.cachedIds.clear();
    }

    public void arrangeForTheRest() {
        // 然后处理自动分配的ID
        for (Map.Entry<String, CompletableFuture<Integer>> entry : this.autoIds.entrySet()) {
            String name = entry.getKey();
            CompletableFuture<Integer> future = entry.getValue();

            // 不应该触发
            if (future.isDone()) {
                continue;
            }

            // 尝试使用缓存的ID，并且其有效
            Integer cachedId = this.cachedIds.get(name);
            if (cachedId != null && !this.occupiedIds.get(cachedId) && cachedId >= this.minId && cachedId <= this.maxId) {
                this.occupiedIds.set(cachedId);
                future.complete(cachedId);
                continue;
            }

            // 寻找下一个可用的自动ID
            int autoId = findNextAvailableAutoId();
            if (autoId == -1) {
                // 没有可用的ID
                future.completeExceptionally(new AutoIdExhaustedException(name, this.minId, this.maxId));
                continue;
            }

            // 分配找到的ID
            this.occupiedIds.set(autoId);
            future.complete(autoId);
            this.cachedIds.put(name, autoId);
        }

        // 清空futureIds，因为所有请求都已处理
        this.autoIds.clear();
    }

    private int findNextAvailableAutoId() {
        // 如果已经用尽
        if (this.currentAutoId > this.maxId) {
            return -1;
        }
        // 寻找下一个可用的id
        this.currentAutoId = this.occupiedIds.nextClearBit(this.currentAutoId);
        // 已经用完了
        if (this.currentAutoId > maxId) {
            return -1;
        }
        // 找到了
        return this.currentAutoId;
    }

    // 强制使用某个id，这时候直接标记到occupiedIds，如果被占用，则直接抛出异常
    public CompletableFuture<Integer> forceId(final String name, int index) {
        // 检查ID是否在有效范围内，一般不会在这触发
        if (index < this.minId || index > this.maxId) {
            return CompletableFuture.failedFuture(new AutoIdOutOfRangeException(name, index, this.minId, this.maxId));
        }

        // 检查ID是否已被其他名称占用
        String previous = this.forcedIds.inverse().get(index);
        if (previous != null && !previous.equals(name)) {
            return CompletableFuture.failedFuture(new AutoIdConflictException(previous, index));
        }

        this.forcedIds.put(name, index);
        this.cachedIds.remove(name); // 如果曾经被缓存过，那么移除
        return CompletableFuture.completedFuture(index);
    }

    // 自动分配id，优先使用缓存的值
    public CompletableFuture<Integer> autoId(final String name) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        this.autoIds.put(name, future);
        return future;
    }

    // 大多数时候通过指令，移除那些已经不再被使用的id，使用完以后记得调用saveCache以保存更改
    public int clearUnusedIds(Predicate<String> predicate) {
        List<String> toRemove = new ArrayList<>();
        for (String id : this.cachedIds.keySet()) {
            if (predicate.test(id)) {
                toRemove.add(id);
            }
        }
        for (String id : toRemove) {
            Integer removedId = this.cachedIds.remove(id);
            if (removedId != null) {
                // 只有当这个ID不是强制ID时才从occupiedIds中移除
                if (!forcedIds.containsValue(removedId)) {
                    occupiedIds.clear(removedId);
                }
            }
        }
        return toRemove.size();
    }

    // 获取已分配的ID（用于调试或查询）
    public Integer getId(String name) {
        if (forcedIds.containsKey(name)) {
            return forcedIds.get(name);
        }
        return cachedIds.get(name);
    }

    // 获取所有已分配的ID映射
    public Map<String, Integer> getAllocatedIds() {
        Map<String, Integer> result = new HashMap<>();
        result.putAll(forcedIds);
        result.putAll(cachedIds);
        return Collections.unmodifiableMap(result);
    }

    // 检查某个ID是否已被占用
    public boolean isIdOccupied(int id) {
        return occupiedIds.get(id);
    }

    // 从缓存中加载文件
    public void loadCache() throws IOException {
        if (!Files.exists(this.cachePath)) {
            return;
        }
        JsonElement element = GsonHelper.readJsonFile(this.cachePath);
        if (element instanceof JsonObject jsonObject) {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue() instanceof JsonPrimitive primitive) {
                    int id = primitive.getAsInt();
                    this.cachedIds.put(entry.getKey(), id);
                }
            }
        }
    }

    // 保存缓存到文件
    public void saveCache() throws IOException {
        FileUtils.createDirectoriesSafe(this.cachePath.getParent());
        GsonHelper.writeJsonFile(GsonHelper.get().toJsonTree(this.cachedIds), this.cachePath);
    }

    public static class AutoIdConflictException extends RuntimeException {
        public AutoIdConflictException(String previousOwner, int id) {
            super("ID " + id + " is already occupied by: " + previousOwner);
        }
    }

    public static class AutoIdOutOfRangeException extends RuntimeException {
        public AutoIdOutOfRangeException(String name, int id, int min, int max) {
            super("ID " + id + " for '" + name + "' is out of range. Valid range: " + min + "-" + max);
        }
    }

    public static class AutoIdExhaustedException extends RuntimeException {
        public AutoIdExhaustedException(String name, int min, int max) {
            super("No available auto ID for '" + name + "'. All IDs in range " + min + "-" + max + " are occupied.");
        }
    }
}