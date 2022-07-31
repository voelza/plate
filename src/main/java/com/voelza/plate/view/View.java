package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.component.Component;
import com.voelza.plate.component.ComponentResolver;
import com.voelza.plate.component.Import;
import com.voelza.plate.html.Element;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class View {
    private final List<Render> renders;
    private final Map<String, View> subViews;

    public View(final String path, final Locale locale) {
        this(
                ComponentResolver.resolve(path, locale)
                        .orElseThrow(() -> new IllegalArgumentException("Could not resolve component " + path))
        );
    }

    private View(final Component component) {
        renders = RenderCreator.create(component.getTemplate().map(Element::children).orElse(Collections.emptyList()));
        subViews = resolveImports(component.getImports());
    }

    private Map<String, View> resolveImports(final List<Import> imports) {
        return null;
    }

    public String render(Model model) {
        final ExpressionResolver expressionResolver = new ExpressionResolver(model);
        
        final StringBuilder html = new StringBuilder();
        for (final Render render : renders) {
            html.append(render.html(expressionResolver));
        }
        return html.toString();
    }
}
