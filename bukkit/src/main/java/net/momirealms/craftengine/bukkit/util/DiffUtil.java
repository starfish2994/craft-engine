package net.momirealms.craftengine.bukkit.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DiffUtil {

    // 对比结果
    public record DiffResult<T>(
            List<T> added,
            List<T> removed
    ) {}

    /**
     * 计算两个列表中新加和丢失的元素。
     * @param previousCollection 可能为null的旧列表
     * @param newCollection 可能为null的新列表
     * @return Map，key "added" 对应新列表中独有元素，key "removed" 对应旧列表中独有元素
     */
    public static <T> DiffResult<T> diff(@Nullable Collection<T> previousCollection, @Nullable Collection<T> newCollection) {
        Set<T> previousSet = previousCollection == null ? Collections.emptySet() : new HashSet<>(previousCollection);
        Set<T> newSet = newCollection == null ? Collections.emptySet() : new HashSet<>(newCollection);

        List<T> added = new ArrayList<>();
        List<T> removed = new ArrayList<>(previousSet);
        removed.removeAll(newSet);

        for (T item : newSet) {
            if (!previousSet.contains(item)) {
                added.add(item);
            }
        }

        return new DiffResult<>(added, removed);
    }

    /**
     * 计算两个列表中共有且未改变的元素。
     * @param oldList 可能为null的旧列表
     * @param newList 可能为null的新列表
     * @return 不变的元素列表
     */
    public static <T> List<T> unchanged(@Nullable List<T> oldList, @Nullable List<T> newList) {
        if (oldList == null || newList == null) {
            return Collections.emptyList();
        }
        Set<T> oldSet = new HashSet<>(oldList);
        List<T> result = new ArrayList<>(newList);
        result.retainAll(oldSet);
        return result;
    }

}
