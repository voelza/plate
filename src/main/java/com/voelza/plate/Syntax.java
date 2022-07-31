package com.voelza.plate;

public enum Syntax {
    TEMPLATED("$$"),
    TEXT_TEMPLATE_REGEX("\\$\\$\\{(.+?)\\}");

    public final String token;

    private Syntax(final String token) {
        this.token = token;
    }
}
