package com.voelza.plate;

import com.voelza.plate.view.View;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Plate {

    private static String templatesPath;
    private static final Map<String, View> VIEWS = new HashMap<>();

    private Plate() {
        // hide
    }

    public static void setTemplatesPath(final String templatesPath) {
        Plate.templatesPath = templatesPath;
    }

    public static String render(final String view, final Locale locale, final Model model) {
        View v = VIEWS.get(view);
        if (v == null) {
            v = new View(templatesPath + "/" + view, locale);
            VIEWS.put(view + locale, v);
        }
        return v.render(model);
    }

    public static Optional<String> getJavaScript(final String view) {
        return Optional.ofNullable(VIEWS.get(view)).map(View::getJavaScript);
    }

    public static Optional<String> getCSS(final String view) {
        return Optional.ofNullable(VIEWS.get(view)).map(View::getCSS);
    }
}
