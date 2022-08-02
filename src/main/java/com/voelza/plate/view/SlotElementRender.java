package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

public class SlotElementRender implements ElementRender {

    final String name;

    SlotElementRender(final Element element) {
        this.name = element.getAttribute("name").map(Attribute::value).orElse("default");
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        throw new IllegalStateException("Oops. This should not have happened");
    }
}
