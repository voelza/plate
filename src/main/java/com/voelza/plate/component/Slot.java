package com.voelza.plate.component;

import com.voelza.plate.html.Element;

import java.util.List;

public record Slot(String name, List<Element> elements) {
}
