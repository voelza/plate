package com.voelza.plate.view;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;

class TemplatedElementRender implements ElementRender {

    private final boolean isStandAloneTag;
    private final Function<ExpressionResolver, String> startingTag;
    private final List<ElementRender> childRenders;
    private final String closingTag;

    TemplatedElementRender(final boolean isStandAloneTag,
                           final Function<ExpressionResolver, String> startingTag,
                           final List<ElementRender> childRenders,
                           final String closingTag) {
        this.isStandAloneTag = isStandAloneTag;
        this.startingTag = startingTag;
        this.childRenders = childRenders;
        this.closingTag = closingTag;
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        final ExpressionResolver expressionResolver = renderContext.expressionResolver();

        List<ScriptPropFill> scriptPropFills = null;
        final StringBuilder html = new StringBuilder();
        html.append(startingTag.apply(expressionResolver));
        if (!isStandAloneTag) {
            final ElementRenderResult result = Renderer.render(childRenders, renderContext);
            html.append(result.html());
            if (result.scriptPropFillsList() != null) {
                scriptPropFills = result.scriptPropFillsList();
            }

            html.append(closingTag);
        }
        return new ElementRenderResult(html.toString(), scriptPropFills);
    }

    @Override
    public ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        final ExpressionResolver expressionResolver = renderContext.expressionResolver();

        List<ScriptPropFill> scriptPropFills = null;
        printWriter.print(startingTag.apply(expressionResolver));
        printWriter.flush();
        if (!isStandAloneTag) {
            final ElementStreamResult result = Renderer.stream(printWriter, childRenders, renderContext);
            if (result.scriptPropFillsList() != null) {
                scriptPropFills = result.scriptPropFillsList();
            }

            printWriter.print(closingTag);
            printWriter.flush();
        }
        return new ElementStreamResult(scriptPropFills);
    }
}
