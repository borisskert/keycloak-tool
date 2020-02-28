package com.github.borisskert.keycloak.config.util;

import java.util.*;

public class SortUtils {
    public static <T> List<T> sorted(List<T> unsorted) {
        ArrayList<T> toSort = new ArrayList<>(unsorted);
        Collections.sort((List) toSort);

        return toSort;
    }

    public static <T> Map<String, List<T>> sorted(Map<String, List<T>> unsorted) {
        Map<String, List<T>> toSort = new HashMap<>(unsorted);

        for (List<T> value : toSort.values()) {
            Collections.sort((List) value);
        }

        return toSort;
    }
}
