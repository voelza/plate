package com.voelza.plate.html;

import java.util.List;
import java.util.Optional;

public class DOM {
    private final List<Element> elements;

    DOM(final List<ParserElement> elements) {
        this.elements = elements.stream().map(this::convertElement).toList();
    }

    private Element convertElement(final ParserElement parserElement) {
        if (parserElement instanceof ParserTextElement textElement) {
            return new TextElement(textElement.getText());
        }

        return new ElementImpl(
                parserElement.name,
                parserElement.attributes,
                parserElement.children.stream().map(this::convertElement).toList(),
                parserElement.isStandAlone);
    }

    public Optional<Element> getFirstElementByName(final String name) {
        return this.elements
                .stream()
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Element> getElementsByName(final String name) {
        return this.elements
                .stream()
                .filter(e -> e.name().equalsIgnoreCase(name))
                .toList();
    }

    public List<Element> getElements() {
        return elements;
    }
}
