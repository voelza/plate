package com.voelza.plate.utils;

public class StringUtils {
    private StringUtils() {

    }

    public static boolean hasText(final String text) {
        return text != null && text.length() > 0;
    }
}
