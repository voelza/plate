package com.voelza.plate.view;

import java.util.function.Function;

class TemplatedTextElementRender implements ElementRender {

    private final Function<ExpressionResolver, String> text;

    TemplatedTextElementRender(final Function<ExpressionResolver, String> text) {
        this.text = text;
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        return new ElementRenderResult(this.text.apply(renderContext.expressionResolver()));
    }
}
