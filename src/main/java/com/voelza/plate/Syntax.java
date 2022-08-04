package com.voelza.plate;

public enum Syntax {
    TEMPLATED("$$"),
    TEXT_TEMPLATE_REGEX("\\$\\$\\{(.+?)\\}"),
    RENDER("render"),
    FOREACH("foreach"),
    UNSAFE("unsafe"),
    SLOT("slot");

    public final String token;

    private Syntax(final String token) {
        this.token = token;
    }
}
