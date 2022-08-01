package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.util.List;
import java.util.Map;

class ConditionalRender implements ElementRender {

    private final String condition;
    private final List<ElementRender> renders;

    ConditionalRender(final Element element, final Map<String, View> subViews, final List<Attribute> additionalDataAttributes) {
        this.condition = element.getAttribute("if")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalStateException("Render element needs 'condition' attribute."));
        this.renders = RenderCreator.create(element.children(), subViews, additionalDataAttributes);
    }

    @Override
    public String renderHTML(final RenderContext renderContext) {
        if (renderContext.expressionResolver().evaluateCondition(this.condition)) {
            return Renderer.render(renders, renderContext);
        }
        return "";
    }
}
