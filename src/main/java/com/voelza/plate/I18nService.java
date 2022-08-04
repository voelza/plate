package com.voelza.plate;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18nService {

    private I18nService() {
        // hide
    }

    private static final ConcurrentHashMap<Locale, Map<String, String>> MESSAGES_BY_LOCALE =
            new ConcurrentHashMap<Locale, Map<String, String>>(20, 0.9f, 2);

    public static void addTranslation(final Locale locale, final Map<String, String> translations) {
        MESSAGES_BY_LOCALE.put(locale, translations);
    }

    public static String translate(final Locale locale, String template) {
        final Locale simpleLocale = Locale.forLanguageTag(locale.getLanguage());
        final Map<String, String> messages = I18nService.getLocaleOrDefault(simpleLocale);
        if (messages != null) {
            final Pattern translationPattern = Pattern.compile("##\\{(\\S+?)}");
            final Matcher matcher = translationPattern.matcher(template);
            if (matcher.find()) {
                template = matcher.replaceAll(result -> {
                    final String key = result.group(1);
                    final String message = messages.get(key);
                    return message != null ? message : "??" + key + "??";
                });
            }
        }
        return template;
    }

    private static Map<String, String> getLocaleOrDefault(final Locale locale) {
        Map<String, String> messages = I18nService.MESSAGES_BY_LOCALE.get(locale);
        if (messages == null) {
            messages = I18nService.MESSAGES_BY_LOCALE.get(Locale.ENGLISH);
        }
        return messages;
    }
}
