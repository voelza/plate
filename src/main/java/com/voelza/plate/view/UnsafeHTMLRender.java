package com.voelza.plate.view;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.HTMLParser;

import java.io.PrintWriter;
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
        final List<ElementRender> renders = getElementRenders(renderContext);
        return Renderer.render(renders, renderContext);
    }

    @Override
    public ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        final List<ElementRender> renders = getElementRenders(renderContext);
        return Renderer.stream(printWriter, renders, renderContext);
    }

    private List<ElementRender> getElementRenders(final RenderContext renderContext) {
        final String html = String.valueOf(renderContext.expressionResolver().evaluate(htmlExpression));
        final List<Element> elements = HTMLParser.parse(html).getElements();
        return RenderCreator.create(options.newElements(elements));
    }
}
