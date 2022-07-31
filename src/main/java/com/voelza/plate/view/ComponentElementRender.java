package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.component.Slot;
import com.voelza.plate.html.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ComponentElementRender implements ElementRender {

    private final View view;
    private final List<PropFill> propFills;
    private final Map<String, SlotFill> slotFills;

    ComponentElementRender(final Element element, final View view, final Map<String, View> subViews) {
        this.view = view;
        propFills = view.props.stream().map(p -> new PropFill(p.name, element)).toList();
        slotFills = createSlotFills(element, view, subViews);
    }

    private static Map<String, SlotFill> createSlotFills(final Element element, final View view, final Map<String, View> subViews) {
        final Map<String, SlotFill> slotFills = new HashMap<>();
        for (final Slot slot : view.slots) {
            slotFills.put(slot.name(), new SlotFill(slot.name(), element, subViews));
        }
        return slotFills;
    }

    @Override
    public String renderHTML(final RenderOptions renderOptions) {
        final Model model = new Model();
        for (final PropFill propFill : propFills) {
            model.add(propFill.name, renderOptions.expressionResolver().evaluate(propFill.propExpression));
        }
        return view.render(model, this.slotFills);
    }
}
