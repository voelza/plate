package com.voelza.plate.view;

import java.util.ArrayList;
import java.util.List;

class Renderer {

    private Renderer() {
        // hide
    }

    static ElementRenderResult render(final List<ElementRender> renders, final RenderContext renderContext) {
        final List<ScriptPropFill> scriptPropFills = new ArrayList<>();
        final StringBuilder html = new StringBuilder();
        for (final ElementRender render : renders) {
            final ElementRenderResult result = render(render, renderContext);
            html.append(result.html());
            if (result.scriptPropFillsList() != null) {
                scriptPropFills.addAll(result.scriptPropFillsList());
            }
        }
        return new ElementRenderResult(html.toString(), scriptPropFills);
    }

    private static ElementRenderResult render(final ElementRender render, final RenderContext renderContext) {
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
