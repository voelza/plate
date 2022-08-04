package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.HTMLParser;

import java.util.List;

class UnsafeHTMLRender implements ElementRender {

    private final String htmlExpression;
    private final RenderCreatorOptions options;

    UnsafeHTMLRender(final Element element, final RenderCreatorOptions options) {
        this.htmlExpression = element.getAttribute("html")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalStateException("Unsafe element needs 'html' attribute."));
        this.options = options;
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        final String html = String.valueOf(renderContext.expressionResolver().evaluate(htmlExpression));
        final List<Element> elements = HTMLParser.parse(html).getElements();
        final List<ElementRender> renders = RenderCreator.create(options.newElements(elements));
        return Renderer.render(renders, renderContext);
    }
}
