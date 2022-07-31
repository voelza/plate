package com.voelza.plate.view;

import java.util.function.Function;

class TemplatedTextElementRender implements ElementRender {

    private final Function<ExpressionResolver, String> text;

    TemplatedTextElementRender(final Function<ExpressionResolver, String> text) {
        this.text = text;
    }

    @Override
    public String renderHTML(final RenderContext renderContext) {
        return this.text.apply(renderContext.expressionResolver());
    }
}
