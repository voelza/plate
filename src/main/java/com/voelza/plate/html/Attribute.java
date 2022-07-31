package com.voelza.plate.html;

import com.voelza.plate.Syntax;

public record Attribute(String name, String value) {
    public boolean isTemplated() {
        return name.startsWith(Syntax.TEMPLATED.token);
    }
}
