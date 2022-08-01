package com.voelza.plate;

public class Version {
    private Version() {
        // hide
    }

    public static String get() {
        final String version = Version.class.getPackage().getImplementationVersion();
        return version != null ? version : "DEV";
    }

}
