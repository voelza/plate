package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.html.Element;

import java.util.List;

class ComponentRender implements Render {

    private final List<PropFill> propFills;
    private final View view;

    ComponentRender(final Element element, final View view) {
        this.view = view;
        propFills = view.props.stream().map(p -> new PropFill(p.name, element)).toList();
    }

    @Override
    public String html(final ExpressionResolver expressionResolver) {
        final Model model = new Model();
        for (final PropFill propFill : propFills) {
            model.add(propFill.name, expressionResolver.evaluate(propFill.propExpression));
        }
        return view.render(model);
    }
}
