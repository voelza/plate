package com.voelza.plate.utils;

import java.util.List;

public class CollectionUtils {
    private CollectionUtils() {
        // hide
    }

    public static boolean isNotEmpty(final List<?> list) {
        return list != null && list.size() > 0;
    }
}
