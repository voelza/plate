package com.voelza.plate.view;

import com.voelza.plate.Syntax;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.TextElement;
import com.voelza.plate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

class RenderCreator {

    private static final Set<String> NONE_ADDITIONAL_ATTRS_ELEMENTS =
            Set.of("!doctype", "html", "head", "title", "script", "style", "link", "media", "meta", "base", "noscript");

    private RenderCreator() {
        // hide
    }

    static List<ElementRender> create(final List<Element> elements,
                                      final Map<String, View> subViews,
                                      final List<Attribute> additionalDataAttributes) {

        final List<ElementRender> renders = new ArrayList<>();
        for (final Element element : elements) {
            if ("render".equalsIgnoreCase(element.name())) {
                renders.add(new ConditionalRender(element, subViews, additionalDataAttributes));
                continue;
            }

            if ("forEach".equalsIgnoreCase(element.name())) {
                renders.add(new ForEachRender(element, subViews, additionalDataAttributes));
                continue;
            }

            final View subView = subViews.get(element.name());
            if (subView != null) {
                renders.add(new ComponentElementRender(element, subView, subViews));
                continue;
            }

            if (element.name().equalsIgnoreCase("slot")) {
                renders.add(new SlotElementRender(element));
                continue;
            }

            if (!element.attributesAreTemplated() && !element.isAnyChildTemplatedOrSubViewOrSlot(subViews)) {
                renders.add(createStaticRender(element, additionalDataAttributes));
                continue;
            }
            renders.add(createTemplatedRender(element, subViews, additionalDataAttributes));
        }
        return renders;
    }

    private static ElementRender createStaticRender(final Element element, final List<Attribute> additionalDataAttributes) {
        return new StaticElementRender(RenderCreator.createStaticHTML(element, additionalDataAttributes));
    }

    private static ElementRender createTemplatedRender(
            final Element element,
            final Map<String, View> subViews,
            final List<Attribute> additionalDataAttributes
    ) {
        if (element instanceof TextElement textElement) {
            // TODO allow HTML
            final String text = textElement.text().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            return new TemplatedTextElementRender((expressionResolver) -> {
                final Pattern p = Pattern.compile(Syntax.TEXT_TEMPLATE_REGEX.token);
                return p.matcher(text).replaceAll(r -> {
                    final String expression = r.group(1);
                    return expressionResolver.evaluate(expression);
                });
            });
        }

        final String staticStartingTag =
                element.attributesAreTemplated() ? null : createStaticStartingTag(element, additionalDataAttributes);
        final List<AttributeRender> attributeRenders = element.attributesAreTemplated()
                ?
                CollectionUtils.union(
                                getAdditionalAttributes(element, additionalDataAttributes),
                                element.attributes()
                        )
                        .stream()
                        .map(a -> {
                            if (a.isTemplated()) {
                                return new TemplatedAttribute((expressionResolver) -> {
                                    final String name = a.name().substring(Syntax.TEMPLATED.token.length());
                                    final String value = expressionResolver.evaluate(a.value());
                                    return createAttribute(name, value);
                                });
                            }
                            return new StaticAttribute(createAttribute(a.name(), a.value()));
                        })
                        .toList()
                : null;

        final Function<ExpressionResolver, String> startingTag = (expressionResolver) -> {
            if (staticStartingTag != null) {
                return staticStartingTag;
            }
            return createTemplatedStartingTag(element.name(), attributeRenders, expressionResolver);
        };

        final boolean isStandAloneTag = element.isStandAloneTag();
        List<ElementRender> childRenders = null;
        String closingTag = null;
        if (!isStandAloneTag) {
            childRenders = create(element.children(), subViews, additionalDataAttributes);
            closingTag = createClosingTag(element);
        }
        return new TemplatedElementRender(isStandAloneTag, startingTag, childRenders, closingTag);
    }

    private static String createStaticHTML(final Element element, final List<Attribute> additionalDataAttributes) {
        if (element instanceof TextElement textElement) {
            return textElement.text();
        }

        final StringBuilder html = new StringBuilder();
        html.append(createStaticStartingTag(element, additionalDataAttributes));

        if (!element.isStandAloneTag()) {
            for (final Element child : element.children()) {
                html.append(createStaticHTML(child, additionalDataAttributes));
            }
            html.append(createClosingTag(element));
        }

        return html.toString();
    }

    private static String createStaticStartingTag(final Element element, final List<Attribute> additionalDataAttributes) {
        final StringBuilder html = new StringBuilder();
        html.append("<");
        html.append(element.name());

        final Collection<Attribute> attributes = CollectionUtils.union(
                getAdditionalAttributes(element, additionalDataAttributes),
                element.attributes()
        );
        if (CollectionUtils.isNotEmpty(attributes)) {
            html.append(" ");
            html.append(
                    String.join(
                            " ",
                            attributes
                                    .stream()
                                    .map(a -> createAttribute(a.name(), a.value()))
                                    .toList()
                    )
            );
        }
        html.append(">");
        return html.toString();
    }

    private static List<Attribute> getAdditionalAttributes(final Element element, final List<Attribute> additionalDataAttributes) {
        return !NONE_ADDITIONAL_ATTRS_ELEMENTS.contains(element.name().toLowerCase()) ? additionalDataAttributes : Collections.emptyList();
    }

    private static String createClosingTag(final Element element) {
        return String.format("</%s>", element.name());
    }

    private static String createAttribute(final String name, final String value) {
        if (value == null) {
            return name;
        }

        return String.format("%s=\"%s\"", name, value);
    }

    private static String createTemplatedStartingTag(final String name,
                                                     final List<AttributeRender> attributeRenders,
                                                     final ExpressionResolver expressionResolver) {
        final StringBuilder html = new StringBuilder();
        html.append("<");
        html.append(name);
        if (CollectionUtils.isNotEmpty(attributeRenders)) {
            html.append(" ");
            html.append(
                    String.join(
                            " ",
                            attributeRenders
                                    .stream()
                                    .map(r -> r.renderAttribute(expressionResolver))
                                    .toList()
                    )
            );
        }
        html.append(">");
        return html.toString();
    }
}
