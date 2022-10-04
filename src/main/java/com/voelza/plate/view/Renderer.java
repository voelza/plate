package com.voelza.plate.view;

import java.io.PrintWriter;
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
                throw new IllegalStateException(String.format("[%s]: Slot %s was not filled.", renderContext.viewName(), slotRender.name));
            }
            return slotFill.render(renderContext);
        }
        return render.renderHTML(renderContext);
    }

    static ElementStreamResult stream(final PrintWriter printWriter, final List<ElementRender> renders, final RenderContext renderContext) {
        final List<ScriptPropFill> scriptPropFills = new ArrayList<>();
        for (final ElementRender render : renders) {
            final ElementStreamResult result = stream(printWriter, render, renderContext);
            if (result.scriptPropFillsList() != null) {
                scriptPropFills.addAll(result.scriptPropFillsList());
            }
        }
        return new ElementStreamResult(scriptPropFills);
    }

    private static ElementStreamResult stream(final PrintWriter printWriter,
                                              final ElementRender render,
                                              final RenderContext renderContext) {
        if (render instanceof SlotElementRender slotRender) {
            final SlotFill slotFill = renderContext.slotFills().get(slotRender.name);
            if (slotFill == null) {
                throw new IllegalStateException(String.format("Slot %s was not filled.", slotRender.name));
            }
            return slotFill.stream(printWriter, renderContext);
        }
        return render.stream(printWriter, renderContext);
    }
}
