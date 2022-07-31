package com.voelza.plate.component;

import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.DOM;
import com.voelza.plate.html.Element;
import com.voelza.plate.html.TextElement;

import java.util.List;
import java.util.Optional;

public class Component {

    private final String path;

    private final List<Import> imports;
    private final List<Prop> props;
    private final Optional<Element> template;
    private final Optional<String> script;
    private final Optional<String> style;

    private final List<Slot> slots;

    Component(final String path, final DOM dom) {
        this.path = path;
        imports = findImports(dom);
        props = findProps(dom);
        template = dom.getFirstElementByName("template");
        script = getTextFromFirstElement(dom, "script");
        style = getTextFromFirstElement(dom, "style");
        slots = findSlots(dom);
    }

    private static Optional<String> getTextFromFirstElement(final DOM dom, final String name) {
        final Optional<Element> script = dom
                .getFirstElementByName(name)
                .flatMap(Element::firstChild);

        if (script.isPresent() && script.get() instanceof TextElement textElement) {
            return Optional.ofNullable(textElement.text());
        }

        return Optional.empty();
    }

    private static List<Import> findImports(final DOM dom) {
        return dom
                .getElementsByName("import")
                .stream()
                .map(Import::new)
                .toList();
    }

    private static List<Prop> findProps(final DOM dom) {
        return dom
                .getElementsByName("prop")
                .stream()
                .map(Prop::new)
                .toList();
    }

    private List<Slot> findSlots(final DOM dom) {
        final List<Slot> slots = dom
                .getElements()
                .stream()
                .map(e -> e.findElementByName("slot"))
                .flatMap(List::stream)
                .map(Component::createSlot)
                .toList();

        boolean foundDefaultSlot = false;
        for (final Slot slot : slots) {
            if (slot.name().equalsIgnoreCase("default")) {
                if (foundDefaultSlot) {
                    throw new IllegalArgumentException(String.format("There are multiple default slots in the component %s", this.path));
                }
                foundDefaultSlot = true;
            }
        }

        return slots;
    }

    private static Slot createSlot(final Element element) {
        final String name = element
                .getAttribute("name")
                .map(Attribute::value)
                .orElse("default");
        return new Slot(name, element.children());
    }

    public String getPath() {
        return path;
    }

    public Optional<Element> getTemplate() {
        return template;
    }

    public Optional<String> getScript() {
        return script;
    }

    public Optional<String> getStyle() {
        return style;
    }

    public List<Import> getImports() {
        return imports;
    }

    public List<Prop> getProps() {
        return props;
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
