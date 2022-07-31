package com.voelza.plate.view;

record StaticRender(String html) implements Render {
    @Override
    public String html(final ExpressionResolver expressionResolver) {
        return html();
    }
}
