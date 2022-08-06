package com.voelza.plate.view;

import com.voelza.plate.html.Element;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

class SlotFill {

    private final List<ElementRender> renders;

    SlotFill(
            final String name,
            final Element element,
            final RenderCreatorOptions options
    ) {
        final List<Element> slotElements = element.firstChildByName(name)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                String.format("Slot %s is not present on element %s", name, element.name())
                        )
                )
                .children();
        renders = RenderCreator.create(options.newElements(slotElements));
    }

    ElementRenderResult render(final RenderContext renderContext) {
        return Renderer.render(renders,
                new RenderContext(
                        ExpressionResolver.merge(renderContext.expressionResolver(), renderContext.parentExpressionResolver()),
                        Collections.emptyMap(),
                        null
                ));
    }

    ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        return Renderer.stream(
                printWriter,
                renders,
                new RenderContext(
                        ExpressionResolver.merge(renderContext.expressionResolver(), renderContext.parentExpressionResolver()),
                        Collections.emptyMap(),
                        null
                ));
    }
}
