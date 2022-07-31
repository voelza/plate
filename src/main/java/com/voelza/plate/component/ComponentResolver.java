package com.voelza.plate.component;

import com.voelza.plate.html.HTMLParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

public class ComponentResolver {

    private ComponentResolver() {
        // hide
    }

    public static Optional<Component> resolve(final String path, final Locale locale) {
        try {
            final String html = loadResultFile(path);
            final Component component = new Component(path, HTMLParser.parse(html));
            // TODO caching etc.
            return Optional.of(component);
        } catch (final Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static String loadResultFile(final String path) throws IOException {
        // TODO this will not work inside jar
        return Files.readString(Paths.get(path)).replaceAll("\\r|\\n|\\t|\\s ", "");
    }
}
