package com.voelza.plate.html;

import java.util.ArrayList;
import java.util.List;

class ParserElement {
    String name;
    List<Attribute> attributes = new ArrayList<>();
    ParserElement parent;
    List<ParserElement> children = new ArrayList<>();
    boolean isStandAlone = false;

    ParserElement() {
    }
}
