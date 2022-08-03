package com.voelza.plate.view;

class UUIDElementRender implements ElementRender {

    private final String uuid;

    UUIDElementRender(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        return new ElementRenderResult("<!--" + uuid + "-->");
    }
}
