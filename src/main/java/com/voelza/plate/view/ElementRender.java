package com.voelza.plate.view;

import java.io.PrintWriter;

interface ElementRender {
    ElementRenderResult renderHTML(RenderContext renderContext);

    default ElementStreamResult stream(PrintWriter printWriter, RenderContext renderContext) {
        final ElementRenderResult renderResult = renderHTML(renderContext);
        printWriter.print(renderResult.html());
        printWriter.flush();
        return new ElementStreamResult(renderResult.scriptPropFillsList());
    }
}
