package com.voelza.plate.view;

import com.voelza.plate.Syntax;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.TextElement;
import com.voelza.plate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

class RenderCreator {
    private RenderCreator() {
        // hide
    }

    static List<Render> create(final List<Element> elements) {
        final List<Render> renders = new ArrayList<>();
        for (final Element element : elements) {
            if (!element.attributesAreTemplated() && !element.isAnyChildTemplated()) {
                renders.add(createStaticRender(element));
            } else {
                renders.add(createTemplatedRender(element));
            }
        }
        return renders;
    }

    private static Render createStaticRender(final Element element) {
        return new StaticRender(RenderCreator.createStaticHTML(element));
    }

    private static Render createTemplatedRender(final Element element) {
        if (element instanceof TextElement textElement) {
            // TODO allow HTML
            final String text = textElement.text().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            return new TemplatedTextRender((expressionResolver) -> {
                final Pattern p = Pattern.compile(Syntax.TEXT_TEMPLATE_REGEX.token);
                return p.matcher(text).replaceAll(r -> {
                    final String expression = r.group(1);
                    return expressionResolver.evaluate(expression);
                });
            });
        }

        final boolean isStandAloneTag = element.isStandAloneTag();
        final String staticStartingTag = element.attributesAreTemplated() ? null : createStaticStartingTag(element);
        final List<Render> attributeRenders = element.attributesAreTemplated()
                ? element
                .attributes()
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

        List<Render> childRenders = null;
        String closingTag = null;
        if (!isStandAloneTag) {
            childRenders = create(element.children());
            closingTag = createClosingTag(element);
        }
        return new TemplatedRender(isStandAloneTag, startingTag, childRenders, closingTag);
    }

    private static String createStaticHTML(final Element element) {
        if (element instanceof TextElement textElement) {
            return textElement.text();
        }

        final StringBuilder html = new StringBuilder();
        html.append(createStaticStartingTag(element));

        if (!element.isStandAloneTag()) {
            for (final Element child : element.children()) {
                html.append(createStaticHTML(child));
            }
            html.append(createClosingTag(element));
        }

        return html.toString();
    }

    private static String createStaticStartingTag(final Element element) {
        final StringBuilder html = new StringBuilder();
        html.append("<");
        html.append(element.name());
        if (CollectionUtils.isNotEmpty(element.attributes())) {
            html.append(" ");
            html.append(
                    String.join(
                            " ",
                            element
                                    .attributes()
                                    .stream()
                                    .map(a -> createAttribute(a.name(), a.value()))
                                    .toList()
                    )
            );
        }
        html.append(">");
        return html.toString();
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
                                                     final List<Render> attributeRenders,
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
                                    .map(r -> r.html(expressionResolver))
                                    .toList()
                    )
            );
        }
        html.append(">");
        return html.toString();
    }
}
