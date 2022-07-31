package com.voelza.plate.view;

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
    public String renderHTML(final RenderOptions renderOptions) {
        final ExpressionResolver expressionResolver = renderOptions.expressionResolver();
        final StringBuilder html = new StringBuilder();
        html.append(startingTag.apply(expressionResolver));
        if (!isStandAloneTag) {
            html.append(Renderer.render(childRenders, renderOptions));
            html.append(closingTag);
        }
        return html.toString();
    }
}
