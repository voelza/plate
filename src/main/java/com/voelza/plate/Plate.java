package com.voelza.plate;

import com.voelza.plate.view.View;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Plate {

    private static String templatesPath = "";
    private static final Map<String, View> VIEWS = new HashMap<>();

    private Plate() {
        // hide
    }

    public static void setTemplatesPath(final String templatesPath) {
        Plate.templatesPath = templatesPath;
    }

    public static void setCustomFileLoader(final Function<String, String> pathToFileContent) {
        FileLoader.setLoadViewFile(pathToFileContent);
    }

    public static void setTranslations(final Supplier<Map<Locale, Map<String, String>>> translationsProvider) {
        translationsProvider.get().forEach(I18nService::addTranslation);
    }

    public static View createView(final String view, final Locale locale) {
        View v = VIEWS.get(view);
        if (v == null) {
            v = new View(templatesPath + view, locale);
            VIEWS.put(view.toLowerCase() + locale, v);
        }
        return v;
    }

    public static Optional<String> getJavaScript(final String view, final Locale locale) {
        return Optional.ofNullable(VIEWS.get(getKey(view, "js", locale))).map(View::getJavaScript);
    }

    public static Optional<String> getCSS(final String view, final Locale locale) {
        return Optional.ofNullable(VIEWS.get(getKey(view, "css", locale))).map(View::getCSS);
    }

    private static String getKey(final String fileName, final String extension, final Locale locale) {
        final String fileEnd = "-" + Version.get() + "." + extension;
        final String viewName = fileName.substring(0, fileName.length() - fileEnd.length());
        return viewName + ".html" + locale;
    }
}
