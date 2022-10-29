package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public record RenderCreatorOptions(
        String viewKey,
        String rootKey,
        AtomicBoolean hasCSS,
        AtomicBoolean hasJavaScript,
        List<Element> elements,
        Map<String, View> subViews,
        Attribute scopeAttribute,
        Attribute setupAttribute) {

    RenderCreatorOptions newElements(final List<Element> elements) {
        return new RenderCreatorOptions(viewKey, rootKey, hasCSS, hasJavaScript, elements, subViews, scopeAttribute, setupAttribute);
    }

    RenderCreatorOptions clearSetupAttribute() {
        return new RenderCreatorOptions(viewKey, rootKey, hasCSS, hasJavaScript, elements, subViews, scopeAttribute, null);
    }
}
