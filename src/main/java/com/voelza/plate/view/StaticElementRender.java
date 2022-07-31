package com.voelza.plate.view;

record StaticElementRender(String html) implements ElementRender {
    @Override
    public String renderHTML(final RenderOptions renderOptions) {
        return html();
    }
}
