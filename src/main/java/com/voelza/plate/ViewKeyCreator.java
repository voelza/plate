package com.voelza.plate;

public class ViewKeyCreator {
    private ViewKeyCreator() {
        // hide
    }

    public static String create(String v) {
        return v
                .toLowerCase()
                .replaceAll("\\.html", "")
                .replaceAll("[\\\\/.]", "_");
    }
}
