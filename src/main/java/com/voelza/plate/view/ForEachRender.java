package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class ForEachRender implements ElementRender {
    private final String collectionExpression;
    private final String elementName;
    private final String indexName;
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
        this.indexName = element.getAttribute("index")
                .map(Attribute::value)
                .orElse(null);
        this.renders = RenderCreator.create(options.newElements(element.children()));
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        final Collection<?> collection = renderContext.expressionResolver().evaluateCollection(this.collectionExpression);

        final List<ScriptPropFill> scriptPropFills = new ArrayList<>();
        final StringBuilder html = new StringBuilder();
        final Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            final Model loopModel = new Model();
            loopModel.add(elementName, iterator.next());
            if (this.indexName != null) {
                loopModel.add(this.indexName, index);
            }
            final ExpressionResolver expressionResolver = renderContext.expressionResolver().withAdditionalModel(loopModel);

            final ElementRenderResult result =
                    Renderer.render(renders, new RenderContext(
                            renderContext.viewName(),
                            expressionResolver,
                            SlotFills.empty(),
                            null
                    ));
            html.append(result.html());
            if (result.scriptPropFillsList() != null) {
                scriptPropFills.addAll(result.scriptPropFillsList());
            }

            index++;
        }

        return new ElementRenderResult(html.toString(), scriptPropFills);
    }

    @Override
    public ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        final Collection<?> collection = renderContext.expressionResolver().evaluateCollection(this.collectionExpression);

        final List<ScriptPropFill> scriptPropFills = new ArrayList<>();
        final Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            final Model loopModel = new Model();
            loopModel.add(elementName, iterator.next());
            if (this.indexName != null) {
                loopModel.add(this.indexName, index);
            }
            final ExpressionResolver expressionResolver = renderContext.expressionResolver().withAdditionalModel(loopModel);

            final ElementStreamResult result = Renderer.stream(
                    printWriter,
                    renders,
                    new RenderContext(
                            renderContext.viewName(),
                            expressionResolver,
                            SlotFills.empty(),
                            null)
            );
            if (result.scriptPropFillsList() != null) {
                scriptPropFills.addAll(result.scriptPropFillsList());
            }

            index++;
        }

        return new ElementStreamResult(scriptPropFills);
    }
}
