package com.voelza.plate.view;

import java.util.function.Function;

class TemplatedAttribute implements AttributeRender {

    private final Function<ExpressionResolver, String> attributeFunction;

    TemplatedAttribute(final Function<ExpressionResolver, String> attributeFunction) {
        this.attributeFunction = attributeFunction;
    }

    @Override
    public String renderAttribute(final ExpressionResolver expressionResolver) {
        return attributeFunction.apply(expressionResolver);
    }
}
