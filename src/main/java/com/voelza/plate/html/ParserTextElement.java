package com.voelza.plate.html;

class ParserTextElement extends ParserElement {

    private final String text;

    ParserTextElement(final String text) {
        this.text = text;
    }

    String getText() {
        return text;
    }
}
