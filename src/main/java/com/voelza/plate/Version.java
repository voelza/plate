package com.voelza.plate;

public class Version {
    private Version() {
        // hide
    }

    private static String version = "DEV";

    static void setVersion(final String version) {
        Version.version = version;
    }

    public static String get() {
        return version;
    }

}
