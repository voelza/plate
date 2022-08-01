package com.voelza.plate.css;

import java.util.Arrays;
import java.util.List;

class CSSBlock {

    private final List<CSSSelector> selectors;
    String style;

    CSSBlock(final String selector) {
        selectors = Arrays.stream(selector.split(",")).map(CSSSelector::new).toList();
    }

    String toCSS(final String scopingName) {
        return String.join(",", selectors.stream().map(s -> s.applyScoping(scopingName)).toList()) + "{" + style + "}";
    }
}
