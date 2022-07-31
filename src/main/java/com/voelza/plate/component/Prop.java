package com.voelza.plate.component;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

class Prop {
    final String name;
    final boolean inScript;

    Prop(final Element propElement) {
        name = propElement
                .getAttribute("name")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalArgumentException("Prop must provide 'name' attribute."));
        inScript = propElement
                .getAttribute("inScript")
                .isPresent();
    }
}
