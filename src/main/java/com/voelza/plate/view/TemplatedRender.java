package com.voelza.plate.view;

import java.util.List;
import java.util.function.Function;

class TemplatedRender implements Render {

    private final boolean isStandAloneTag;
    private final Function<ExpressionResolver, String> startingTag;
    private final List<Render> childRenders;
    private final String closingTag;

    TemplatedRender(final boolean isStandAloneTag,
                    final Function<ExpressionResolver, String> startingTag,
                    final List<Render> childRenders,
                    final String closingTag) {
        this.isStandAloneTag = isStandAloneTag;
        this.startingTag = startingTag;
        this.childRenders = childRenders;
        this.closingTag = closingTag;
    }

    @Override
    public String html(final ExpressionResolver expressionResolver) {
        final StringBuilder html = new StringBuilder();
        html.append(startingTag.apply(expressionResolver));
        if (!isStandAloneTag) {
            html.append(String.join("", childRenders.stream().map(c -> c.html(expressionResolver)).toList()));
            html.append(closingTag);
        }
        return html.toString();
    }
}
