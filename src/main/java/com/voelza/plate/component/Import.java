package com.voelza.plate.component;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;

public class Import {
    public final String file;
    public final String name;

    Import(final Element importElement) {
        file = importElement
                .getAttribute("file")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalArgumentException("Import of components must provide 'file' attribute."));
        name = importElement
                .getAttribute("name")
                .map(Attribute::value)
                .orElseThrow(() -> new IllegalArgumentException("Import of components must provide 'name' attribute."));
    }

}
