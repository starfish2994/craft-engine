package net.momirealms.craftengine.core.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
    private ListUtils() {
    }

    @SafeVarargs
    public static <T> List<T> newNonNullList(T... elements) {
        List<T> list = new ArrayList<>(elements.length);
        for (T element : elements) {
            if (element != null) list.add(element);
        }
        return list;
    }
}
