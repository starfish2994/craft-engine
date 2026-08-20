package net.momirealms.craftengine.core.util;

import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlUtils {

    private YamlUtils() {
    }

    public static Map<String, Object> toMap(Section section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            map.put(entry.getKey(), toPlainValue(entry.getValue()));
        }
        return map;
    }

    public static Object toPlainValue(Object value) {
        if (value instanceof Section section) {
            return toMap(section);
        }
        if (value instanceof List<?> list) {
            List<Object> converted = new ArrayList<>(list.size());
            for (Object element : list) {
                converted.add(toPlainValue(element));
            }
            return converted;
        }
        return value;
    }
}
