package com.voelza.plate.view;

record StaticElementRender(String html) implements ElementRender {
    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        return new ElementRenderResult(html);
    }
}
