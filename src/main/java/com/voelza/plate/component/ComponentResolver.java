package com.voelza.plate.component;

import com.voelza.plate.FileLoader;
import com.voelza.plate.html.HTMLParser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ComponentResolver {

    private static final Map<String, Component> COMPONENT_MAP = new HashMap<>();

    private ComponentResolver() {
        // hide
    }

    public static Optional<Component> resolve(final String path, final Locale locale) {
        try {
            final String key = path + locale.toString();
            Component component = COMPONENT_MAP.get(key);
            if (component == null) {
                final String html = FileLoader.loadViewFile(path);
                // TODO i18n
                component = new Component(path, HTMLParser.parse(html));
                COMPONENT_MAP.put(key, component);
            }
            return Optional.of(component);
        } catch (final Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
