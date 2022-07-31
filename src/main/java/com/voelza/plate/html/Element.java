package com.voelza.plate.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface Element {
    default String name() {
        return "";
    }

    default List<Attribute> attributes() {
        return Collections.emptyList();
    }

    default List<Element> children() {
        return Collections.emptyList();
    }

    default boolean isStandAloneTag() {
        return false;
    }

    default boolean attributesAreTemplated() {
        return attributes().stream().anyMatch(Attribute::isTemplated);
    }

    default boolean isAnyChildTemplated() {
        return children().stream().anyMatch(c -> c.attributesAreTemplated() || c.isAnyChildTemplated());
    }

    default Optional<Attribute> getAttribute(final String name) {
        return attributes().stream().filter(a -> a.name().equalsIgnoreCase(name)).findFirst();
    }

    default List<Element> findChildrenByName(final String name) {
        return children()
                .stream()
                .filter(c -> c.name().equalsIgnoreCase(name))
                .toList();
    }

    default List<Element> findElementByName(final String name) {
        final List<Element> foundElements = new ArrayList<>(findChildrenByName(name)
                .stream()
                .map(c -> c.findChildrenByName(name))
                .flatMap(List::stream)
                .toList());

        if (name().equalsIgnoreCase(name)) {
            foundElements.add(this);
        }

        return foundElements;

    }

    default Optional<Element> firstChild() {
        return children().stream().findFirst();
    }
}
