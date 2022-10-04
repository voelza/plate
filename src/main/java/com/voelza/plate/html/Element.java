package com.voelza.plate.html;

import com.voelza.plate.Syntax;
import com.voelza.plate.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    default boolean isAnyChildTemplatedOrSubViewOrSpecialTag(final Map<String, View> subViews) {
        return children().stream()
                .anyMatch(
                        c -> c.attributesAreTemplated()
                                || c.name().equalsIgnoreCase(Syntax.SLOT.token)
                                || c.name().equalsIgnoreCase(Syntax.UNSAFE.token)
                                || c.name().equalsIgnoreCase(Syntax.FOREACH.token)
                                || c.name().equalsIgnoreCase(Syntax.RENDER.token)
                                || c.name().equalsIgnoreCase(Syntax.HEAD.token)
                                || subViews.get(c.name()) != null
                                || c.isAnyChildTemplatedOrSubViewOrSpecialTag(subViews)
                );
    }

    default Optional<Attribute> getAttribute(final String name) {
        return attributes().stream().filter(a -> a.name().equalsIgnoreCase(name)).findFirst();
    }

    default List<Element> findElementsByName(final String name) {
        final List<Element> foundElements = new ArrayList<>(
                children()
                        .stream()
                        .map(c -> c.findElementsByName(name))
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

    default Optional<Element> firstChildByName(final String name) {
        return children().stream().filter(c -> c.name().equalsIgnoreCase(name)).findFirst();
    }
}
