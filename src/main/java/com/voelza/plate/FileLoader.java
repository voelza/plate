package com.voelza.plate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class FileLoader {

    private static Function<String, String> loadViewFile;

    private FileLoader() {
        // hide
    }

    static void setLoadViewFile(final Function<String, String> pathToFileContent) {
        FileLoader.loadViewFile = pathToFileContent;
    }

    public static String loadViewFile(final String path) throws IOException {
        String code;
        if (loadViewFile != null) {
            code = loadViewFile.apply(path);
        } else {
            code = Files.readString(Paths.get(path));
        }
        return code.replaceAll("\\r|\\n|\\t|\\s ", "");
    }
}
