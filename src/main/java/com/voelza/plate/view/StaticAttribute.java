package com.voelza.plate.view;

public record StaticAttribute(String html) implements AttributeRender {
    @Override
    public String renderAttribute(final ExpressionResolver expressionResolver) {
        return html();
    }
}
