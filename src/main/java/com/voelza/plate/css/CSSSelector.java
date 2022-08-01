package com.voelza.plate.css;

import java.util.ArrayList;
import java.util.List;

class CSSSelector {

    private final String selector;

    CSSSelector(final String selector) {
        this.selector = selector.strip();
    }

    String applyScoping(final String scoping) {
        final List<String> tokens = new ArrayList<>();
        int i = 0;
        String buffer = "";
        while (i < selector.length()) {
            final char c = selector.charAt(i);
            buffer += c;
            if (c == '.') {
                if (buffer.endsWith(".") && buffer.length() > 1) {
                    tokens.add(buffer.substring(0, buffer.length() - 1));
                    tokens.add(".");
                } else {
                    tokens.add(buffer);
                }
                buffer = "";
            }
            i++;
            if (i == selector.length()) {
                tokens.add(buffer);
            }
        }

        return String.join("",
                tokens
                        .stream()
                        .map(s -> {
                            if (!".".equals(s)) {
                                return s + "[" + scoping + "]";
                            }
                            return s;
                        })
                        .toList());
    }

}
