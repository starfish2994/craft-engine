package net.momirealms.craftengine.core.util;

import java.util.*;

public class MiscUtils {

    private MiscUtils() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> castToMap(Object obj, boolean allowNull) {
        if (allowNull && obj == null) {
            return null;
        }
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Expected Map, got: " + (obj == null ? null : obj.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getAsMapList(Object obj) {
        if (obj == null) return List.of();
        if (obj instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        } else if (obj instanceof Map<?, ?>) {
            return List.of((Map<String, Object>) obj);
        }
        throw new IllegalArgumentException("Expected MapList/Map, got: " + obj.getClass().getSimpleName());
    }

    public static List<String> getAsStringList(Object o) {
        List<String> list = new ArrayList<>();
        if (o instanceof List<?>) {
            for (Object object : (List<?>) o) {
                list.add(object.toString());
            }
        } else if (o instanceof String) {
            list.add((String) o);
        } else {
            if (o != null) {
                list.add(o.toString());
            }
        }
        return list;
    }

    public static String[] getAsStringArray(Object o) {
        if (o instanceof List<?> list) {
            String[] array = new String[list.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = list.get(i).toString();
            }
            return array;
        } else if (o != null) {
            return new String[]{o.toString()};
        } else {
            return new String[0];
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAsList(Object o, Class<T> clazz) {
        if (o instanceof List<?> list) {
            if (list.isEmpty()) {
                return List.of();
            }
            if (clazz.isInstance(list.getFirst())) {
                return (List<T>) list;
            }
        }
        if (clazz.isInstance(o)) {
            return List.of((T) o);
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public static void deepMergeMaps(Map<String, Object> baseMap, Map<String, Object> mapToMerge) {
        for (Map.Entry<String, Object> entry : mapToMerge.entrySet()) {
            String key = entry.getKey();
            if (key.length() > 2 && key.charAt(0) == '$' && key.charAt(1) == '$') {
                Object value = entry.getValue();
                baseMap.put(key.substring(1), value);
            } else {
                Object value = entry.getValue();
                if (baseMap.containsKey(key)) {
                    Object existingValue = baseMap.get(key);
                    if (existingValue instanceof Map && value instanceof Map) {
                        Map<String, Object> existingMap = (Map<String, Object>) existingValue;
                        Map<String, Object> newMap = (Map<String, Object>) value;
                        deepMergeMaps(existingMap, newMap);
                    } else if (existingValue instanceof List && value instanceof List) {
                        List<Object> existingList = (List<Object>) existingValue;
                        List<Object> newList = (List<Object>) value;
                        existingList.addAll(newList);
                    } else {
                        baseMap.put(key, value);
                    }
                } else {
                    baseMap.put(key, value);
                }
            }
        }
    }

    public static <T> T requireNonNullIf(T o, boolean condition) {
        if (condition) {
            return Objects.requireNonNull(o);
        } else {
            return o;
        }
    }

    public static boolean matchRegex(String id, Set<String> ids, boolean regexMatch) {
        if (regexMatch) {
            for (String regex : ids) {
                if (id.matches(regex)) {
                    return true;
                }
            }
        } else {
            return ids.contains(id);
        }
        return false;
    }
}
