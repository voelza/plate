package com.voelza.plate.html;

import com.voelza.plate.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTMLParser {

    private HTMLParser() {
        // hide
    }

    private static final List<String> STAND_ALONE_TAGS = Arrays.asList(
            "!doctype",
            "area",
            "base",
            "br",
            "col",
            "embed",
            "hr",
            "img",
            "input",
            "keygen",
            "link",
            "meta",
            "param",
            "source",
            "track",
            "wbr",
            "import",
            "prop",
            "unsafe",
            "slot");

    private static boolean isStandaloneTag(final String name) {
        return HTMLParser.STAND_ALONE_TAGS.contains(name.toLowerCase());
    }

    private static char peek(final String template, final int currentIndex) {
        final int nextIndex = currentIndex + 1;
        if (nextIndex < template.length()) {
            return template.charAt(nextIndex);
        }
        throw new IllegalArgumentException();
    }

    private static char previous(final String template, final int currentIndex) {
        final int nextIndex = currentIndex - 1;
        if (nextIndex >= 0) {
            return template.charAt(nextIndex);
        }
        throw new IllegalArgumentException();
    }

    public static DOM parse(final String html) {
        final List<ParserElement> elements = new ArrayList<>();
        final String template = html.trim();
        int i = 0;
        ParserElement currentElement = null;
        while (i < template.length()) {
            char c = template.charAt(i);

            if (c == '<' && peek(template, i) == '/') {
                final ConsumeResult<String> name = consumeName(i + 1, template);
                currentElement = currentElement.parent;
                i = name.newIndex;

                final ConsumeResult<String> text = consumeTextNode(i + 1, template);
                if (StringUtils.hasText(text.value)) {
                    currentElement.children.add(new ParserTextElement(text.value));
                    i = text.newIndex;
                }
            } else if (c == '<') {
                ParserElement parentElement = currentElement;
                currentElement = new ParserElement();
                currentElement.parent = parentElement;
                if (currentElement.parent == null) {
                    elements.add(currentElement);
                } else {
                    parentElement.children.add(currentElement);
                }

                final ConsumeResult<String> name = consumeName(i, template);
                currentElement.name = name.value;
                i = name.newIndex + 1;
                c = template.charAt(i);

                if (c != '>' && !(c == '/' && peek(template, i) == '>')) {
                    final ConsumeResult<List<Attribute>> attributes = consumeAttributes(i, template);
                    currentElement.attributes = attributes.value;
                    i = attributes.newIndex;
                }

                if (isStandaloneTag(currentElement.name)) {
                    currentElement.isStandAlone = true;
                    i = template.indexOf(">", i);
                    currentElement = currentElement.parent;
                    if (currentElement != null) {
                        parentElement = currentElement.parent;
                    }
                } else {
                    parentElement = currentElement;
                }

                final ConsumeResult<String> text = consumeTextNode(i, template);
                if (StringUtils.hasText(text.value)) {
                    currentElement.children.add(new ParserTextElement(text.value));
                    i = text.newIndex;
                }
            }

            i++;
        }
        return new DOM(elements);
    }

    private static ConsumeResult<String> consumeName(final int currentIndex, final String template) {
        return consumeUntil(currentIndex, template, List.of(' ', '/', '>'));
    }

    private static ConsumeResult<List<Attribute>> consumeAttributes(final int currentIndex, final String template) {
        final List<Attribute> attributes = new ArrayList<>();
        int i = currentIndex;
        boolean parsingName = false;
        StringBuilder nameBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        boolean parsingValue = false;
        while (true) {
            final char c = template.charAt(i);

            if (!parsingValue && (c == '>' || c == '/')) {
                if (StringUtils.hasText(nameBuffer.toString())) {
                    attributes.add(new Attribute(nameBuffer.toString(), null));
                }
                break;
            }

            if (!parsingValue && !parsingName && c != ' ') {
                parsingName = true;
            }

            if (parsingName) {
                if (c == '=') {
                    parsingName = false;
                    parsingValue = true;
                    if (peek(template, i) == '"') {
                        i++; // skip quote mark
                    }
                } else if (c == ' ') {
                    attributes.add(new Attribute(nameBuffer.toString(), null));
                    nameBuffer = new StringBuilder();
                    parsingName = false;
                } else {
                    nameBuffer.append(c);
                }
            } else if (parsingValue) {
                if (c == '"' && previous(template, i) != '\\') {
                    parsingValue = false;
                    attributes.add(new Attribute(nameBuffer.toString(), valueBuffer.toString()));
                    nameBuffer = new StringBuilder();
                    valueBuffer = new StringBuilder();
                    if (peek(template, i) == '"') {
                        i++; // skip quote mark
                    }
                } else {
                    valueBuffer.append(c);
                }
            }

            i++;
            if (i >= template.length()) {
                break;
            }
        }

        return new ConsumeResult<>(attributes, i);
    }

    private static ConsumeResult<String> consumeTextNode(final int currentIndex, final String template) {
        return consumeUntil(currentIndex, template, List.of('<'));
    }

    private static ConsumeResult<String> consumeUntil(final int currentIndex, final String template, final List<Character> endChars) {
        int i = currentIndex + 1;
        String value = "";
        while (true) {
            if (i >= template.length()) {
                break;
            }

            final char c = template.charAt(i);
            if (endChars.contains(c)) {
                break;
            }
            value += c;

            i++;
        }
        return new ConsumeResult<>(value, currentIndex + value.length());
    }

    private record ConsumeResult<V>(V value, int newIndex) {

    }
}
