package com.voelza.plate.view;

import java.util.List;

class Renderer {

    private Renderer() {
        // hide
    }

    static String render(final List<ElementRender> renders, final RenderOptions renderOptions) {
        final StringBuilder html = new StringBuilder();
        for (final ElementRender render : renders) {
            html.append(render(render, renderOptions));
        }
        return html.toString();
    }

    private static String render(final ElementRender render, final RenderOptions renderOptions) {
        if (render instanceof SlotElementRender slotRender) {
            final SlotFill slotFill = renderOptions.slotFills().get(slotRender.name);
            if (slotFill == null) {
                throw new IllegalStateException(String.format("Slot %s was not filled.", slotRender.name));
            }
            return slotFill.render(renderOptions.expressionResolver());
        }
        return render.renderHTML(renderOptions);
    }
}
