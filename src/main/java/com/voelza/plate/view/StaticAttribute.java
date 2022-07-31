package com.voelza.plate.view;

public record StaticAttribute(String html) implements Render {
    @Override
    public String html(final ExpressionResolver expressionResolver) {
        return html();
    }
}
