package com.voelza.plate.view;

import java.util.List;

public record ElementRenderResult(String html, List<ScriptPropFill> scriptPropFillsList) {
    public ElementRenderResult(final String html) {
        this(html, null);
    }
}
