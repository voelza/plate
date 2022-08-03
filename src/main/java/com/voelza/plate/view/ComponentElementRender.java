package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.component.Slot;
import com.voelza.plate.html.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class ComponentElementRender implements ElementRender {

    private final View view;
    private final List<PropFill> propFills;
    private final Map<String, SlotFill> slotFills;

    ComponentElementRender(
            final Element element,
            final View view,
            final RenderCreatorOptions options
    ) {
        this.view = view;
        propFills = view.props.stream().map(p -> new PropFill(p.name, p.inScript, element)).toList();
        slotFills = createSlotFills(element, view, options);
    }

    private static Map<String, SlotFill> createSlotFills(
            final Element element,
            final View view,
            final RenderCreatorOptions options
    ) {
        final Map<String, SlotFill> slotFills = new HashMap<>();
        for (final Slot slot : view.slots) {
            slotFills.put(slot.name(), new SlotFill(slot.name(), element, options));
        }
        return slotFills;
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        final String uuid = UUID.randomUUID().toString();
        final Model model = new Model();
        final List<ScriptPropFill> scriptPropFills = new ArrayList<>();
        for (final PropFill propFill : propFills) {
            final Object propValue = renderContext.expressionResolver().evaluate(propFill.propExpression);
            model.add(propFill.name, propValue);
            if (propFill.isScriptProp) {
                scriptPropFills.add(new ScriptPropFill(uuid, propFill.name, propValue));
            }
        }

        final String html =
                view.render(scriptPropFills.size() > 0 ? uuid : null, model, this.slotFills, renderContext.expressionResolver());
        return new ElementRenderResult(html, scriptPropFills);
    }
}
