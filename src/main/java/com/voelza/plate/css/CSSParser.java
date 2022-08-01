package com.voelza.plate.css;

import java.util.ArrayList;
import java.util.List;

public class CSSParser {
    private CSSParser() {
        // hide
    }

    public static String scopeCSS(final String css, final String scopingName) {
        final List<CSSBlock> cssBlocks = new ArrayList<>();

        int i = 0;
        StringBuilder buffer = new StringBuilder();
        CSSBlock cssBlock = null;
        while (i < css.length()) {
            final char c = css.charAt(i);
            if (c == '{') {
                cssBlock = new CSSBlock(buffer.toString());
                cssBlocks.add(cssBlock);

                buffer = new StringBuilder();
            } else if (c == '}') {
                assert cssBlock != null;
                cssBlock.style = buffer.toString();

                buffer = new StringBuilder();
                cssBlock = null;
            } else {
                buffer.append(c);
            }
            i++;
        }

        return String.join("",
                cssBlocks.stream()
                        .map(b -> b.toCSS(scopingName))
                        .toList());
    }
}
