package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.util.List;
import java.util.Map;

public record RenderCreatorOptions(
        String viewName,
        boolean hasCSS,
        boolean hasJavaScript,
        List<Element> elements,
        Map<String, View> subViews,
        List<Attribute> additionalDataAttributes) {

    RenderCreatorOptions newElements(final List<Element> elements) {
        return new RenderCreatorOptions(viewName, hasCSS, hasJavaScript, elements, subViews, additionalDataAttributes);
    }
}
