package com.voelza.plate.view;

import java.util.function.Function;

class TemplatedTextElementRender implements ElementRender {

    private final Function<ExpressionResolver, String> text;

    TemplatedTextElementRender(final Function<ExpressionResolver, String> text) {
        this.text = text;
    }

    @Override
    public String renderHTML(final RenderOptions renderOptions) {
        return this.text.apply(renderOptions.expressionResolver());
    }
}
