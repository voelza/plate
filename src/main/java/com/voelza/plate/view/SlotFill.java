package com.voelza.plate.view;

import com.voelza.plate.html.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class SlotFill {

    private final List<ElementRender> renders;

    SlotFill(final String name, final Element element, Map<String, View> subViews) {
        final List<Element> slotElements = element.firstChildByName(name)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                String.format("Slot %s is not present on element %s", name, element.name())
                        )
                )
                .children();
        renders = RenderCreator.create(slotElements, subViews);
    }

    String render(final ExpressionResolver expressionResolver) {
        return Renderer.render(renders, new RenderOptions(expressionResolver, Collections.emptyMap()));
    }
}
