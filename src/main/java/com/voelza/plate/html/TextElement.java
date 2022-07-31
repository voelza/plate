package com.voelza.plate.html;

import com.voelza.plate.Syntax;

public record TextElement(String text) implements Element {

    @Override
    public boolean attributesAreTemplated() {
        return text.contains(Syntax.TEMPLATED.token);
    }
}
