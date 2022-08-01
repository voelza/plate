package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class ForEachRender implements ElementRender {
    private final String collectionExpression;
    private final String elementName;
    private final List<ElementRender> renders;

    ForEachRender(
            final Element element,
            final RenderCreatorOptions options
    ) {
        this.collectionExpression = element.getAttribute("collection")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalStateException("ForEach element needs 'collection' attribute."));
        this.elementName = element.getAttribute("element")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalStateException("ForEach element needs 'element' attribute."));
        this.renders = RenderCreator.create(options.newElements(element.children()));
    }

    @Override
    public String renderHTML(final RenderContext renderContext) {
        final Collection<?> collection = renderContext.expressionResolver().evaluateCollection(this.collectionExpression);

        final StringBuilder html = new StringBuilder();

        final Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {

            final Model loopModel = new Model();
            loopModel.add(elementName, iterator.next());
            loopModel.add("_index", index);
            final ExpressionResolver expressionResolver = renderContext.expressionResolver().withAdditionalModel(loopModel);
            html.append(Renderer.render(renders, new RenderContext(expressionResolver, Collections.emptyMap(), null)));
            index++;
        }

        return html.toString();
    }
}
