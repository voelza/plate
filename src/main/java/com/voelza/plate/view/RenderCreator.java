package com.voelza.plate.view;

import com.voelza.plate.Syntax;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.TextElement;
import com.voelza.plate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

class RenderCreator {

    private static final Set<String> NONE_ADDITIONAL_ATTRS_ELEMENTS =
            Set.of("!doctype", "html", "title", "script", "style", "link", "media", "meta", "base", "noscript");

    private static final Set<String> NONE_SETUP_ATTRS = Set.of("!doctype", "noscript");

    private RenderCreator() {
        // hide
    }

    static List<ElementRender> create(final RenderCreatorOptions options) {
        final List<ElementRender> renders = new ArrayList<>();
        for (final Element element : options.elements()) {
            if ("render".equalsIgnoreCase(element.name())) {
                renders.add(new ConditionalRender(element, options));
                continue;
            }

            if ("forEach".equalsIgnoreCase(element.name())) {
                renders.add(new ForEachRender(element, options));
                continue;
            }

            if ("head".equalsIgnoreCase(element.name())) {
                renders.add(new HeadElementRender(element, options));
                continue;
            }

            final View subView = options.subViews().get(element.name());
            if (subView != null) {
                renders.add(new ComponentElementRender(element, subView, options));
                continue;
            }

            if (element.name().equalsIgnoreCase("slot")) {
                renders.add(new SlotElementRender(element));
                continue;
            }

            if (!element.attributesAreTemplated() && !element.isAnyChildTemplatedOrSubViewOrSlot(options.subViews())) {
                renders.add(createStaticRender(element, options));
                continue;
            }
            renders.add(createTemplatedRender(element, options));
        }
        return renders;
    }

    private static ElementRender createStaticRender(
            final Element element,
            final RenderCreatorOptions options
    ) {
        return new StaticElementRender(RenderCreator.createStaticHTML(element, options));
    }

    private static ElementRender createTemplatedRender(
            final Element element,
            final RenderCreatorOptions options
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

        final List<AttributeRender> attributeRenders = element.attributesAreTemplated()
                ?
                CollectionUtils.union(
                                getAdditionalAttributes(element, options),
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

        final String staticStartingTag = element.attributesAreTemplated() ? null : createStaticStartingTag(element, options);
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
            childRenders = create(options.newElements(element.children()).clearSetupAttribute());
            closingTag = createClosingTag(element);
        }
        return new TemplatedElementRender(isStandAloneTag, startingTag, childRenders, closingTag);
    }

    private static String createStaticHTML(
            final Element element,
            final RenderCreatorOptions options
    ) {
        if (element instanceof TextElement textElement) {
            return textElement.text();
        }

        final StringBuilder html = new StringBuilder();
        html.append(createStaticStartingTag(element, options));

        if (!element.isStandAloneTag()) {
            for (final Element child : element.children()) {
                html.append(createStaticHTML(child, options.clearSetupAttribute()));
            }
            html.append(createClosingTag(element));
        }

        return html.toString();
    }

    private static String createStaticStartingTag(final Element element,
                                                  final RenderCreatorOptions options) {
        final StringBuilder html = new StringBuilder();
        html.append("<");
        html.append(element.name());

        final Collection<Attribute> attributes = CollectionUtils.union(
                getAdditionalAttributes(element, options),
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

    private static List<Attribute> getAdditionalAttributes(final Element element, final RenderCreatorOptions options) {
        final List<Attribute> additionalAttributes = new ArrayList<>();
        if (options.scopeAttribute() != null && !NONE_ADDITIONAL_ATTRS_ELEMENTS.contains(element.name().toLowerCase())) {
            additionalAttributes.add(options.scopeAttribute());
        }
        if (options.setupAttribute() != null && !NONE_SETUP_ATTRS.contains(element.name().toLowerCase())) {
            additionalAttributes.add(options.setupAttribute());
        }

        return additionalAttributes;
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
