package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.io.PrintWriter;
import java.util.List;

class ConditionalRender implements ElementRender {

    private final String condition;
    private final List<ElementRender> renders;

    ConditionalRender(
            final Element element,
            final RenderCreatorOptions options
    ) {
        this.condition = element.getAttribute("if")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalStateException("Render element needs 'condition' attribute."));
        this.renders = RenderCreator.create(options.newElements(element.children()));
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        if (renderContext.expressionResolver().evaluateCondition(this.condition)) {
            return Renderer.render(renders, renderContext);
        }
        return new ElementRenderResult("");
    }

    @Override
    public ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        if (renderContext.expressionResolver().evaluateCondition(this.condition)) {
            return Renderer.stream(printWriter, renders, renderContext);
        }
        return new ElementStreamResult(null);
    }
}
