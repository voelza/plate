package com.voelza.plate.html;

import java.util.List;

public record ElementImpl(String name, List<Attribute> attributes, List<Element> children, boolean isStandAloneTag) implements Element {
}
