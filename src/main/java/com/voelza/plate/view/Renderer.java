package com.voelza.plate.view;

import java.util.List;

class Renderer {

    private Renderer() {
        // hide
    }

    static String render(final List<ElementRender> renders, final RenderContext renderContext) {
        final StringBuilder html = new StringBuilder();
        for (final ElementRender render : renders) {
            html.append(render(render, renderContext));
        }
        return html.toString();
    }

    private static String render(final ElementRender render, final RenderContext renderContext) {
        if (render instanceof SlotElementRender slotRender) {
            final SlotFill slotFill = renderContext.slotFills().get(slotRender.name);
            if (slotFill == null) {
                throw new IllegalStateException(String.format("Slot %s was not filled.", slotRender.name));
            }
            return slotFill.render(renderContext);
        }
        return render.renderHTML(renderContext);
    }
}
