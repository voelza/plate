package com.voelza.plate.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionUtils {
    private CollectionUtils() {
        // hide
    }

    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    @SafeVarargs
    public static <T> Collection<T> union(final Collection<T>... collections) {
        final Set<T> set = new HashSet<T>();

        for (final Collection<T> collection : collections) {
            set.addAll(collection);
        }

        return new ArrayList<T>(set);
    }

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }
}
