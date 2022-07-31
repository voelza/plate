package com.voelza.plate.view;

import com.voelza.plate.Syntax;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

class PropFill {

    final String name;
    final String propExpression;

    PropFill(final String propName, final Element element) {
        name = propName;
        this.propExpression = element
                .getAttribute(Syntax.TEMPLATED.token + propName)
                .map(Attribute::value)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Prop '%s' was defined but is not set in your template.", propName))
                );
    }
}
