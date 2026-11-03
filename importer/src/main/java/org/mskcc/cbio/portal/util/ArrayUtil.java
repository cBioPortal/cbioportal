package org.mskcc.cbio.portal.util;

import java.util.*;

public class ArrayUtil {
    public static <K, V> Map<K, V> zip(K[] keys, V[] values) {
        Map<K, V> map = new HashMap<>();

        // Check if both arrays have the same length
        if (keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }
        } else {
            throw new IllegalArgumentException("Arrays must be of the same length");
        }
        return map;

    }
}
