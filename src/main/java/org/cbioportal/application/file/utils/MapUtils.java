package org.cbioportal.application.file.utils;

import java.util.AbstractMap;
import java.util.Map;

public class MapUtils {
    /**
     * Create a key-value entry that allows null values
     * Map.entry does not allow null values
     */
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }
}
