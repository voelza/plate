package com.voelza.plate.view;

import java.util.function.Function;

class TemplatedTextRender implements Render {

    private final Function<ExpressionResolver, String> text;

    TemplatedTextRender(final Function<ExpressionResolver, String> text) {
        this.text = text;
    }

    @Override
    public String html(final ExpressionResolver expressionResolver) {
        return this.text.apply(expressionResolver);
    }
}
