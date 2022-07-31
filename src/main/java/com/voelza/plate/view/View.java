package com.voelza.plate.view;

import com.voelza.plate.Model;
import com.voelza.plate.component.Component;
import com.voelza.plate.component.ComponentResolver;
import com.voelza.plate.component.Import;
import com.voelza.plate.component.Prop;
import com.voelza.plate.component.Slot;
import com.voelza.plate.html.Element;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class View {

    private final String directoryPath;
    private final Locale locale;
    final List<ElementRender> renders;
    final Map<String, View> subViews;

    final List<Prop> props;
    final List<Slot> slots;

    public View(final String path, final Locale locale) {
        this.directoryPath = Path.of(path).getParent().toString();
        this.locale = locale;

        final Component component = getComponent(path, locale);
        subViews = resolveImports(component.getImports());
        props = component.getProps();
        slots = component.getSlots();
        final List<Element> elements = component.getTemplate().map(Element::children).orElse(Collections.emptyList());
        renders = RenderCreator.create(elements, subViews);
    }

    private static Component getComponent(final String path, final Locale locale) {
        return ComponentResolver.resolve(path, locale)
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve component " + path));
    }

    private Map<String, View> resolveImports(final List<Import> imports) {
        final Map<String, View> subView = new HashMap<>();
        for (final Import i : imports) {
            subView.put(i.name.toLowerCase(), new View(directoryPath + "/" + i.file, locale));
        }
        return subView;
    }

    public String render(final Model model) {
        return render(model, Collections.emptyMap(), null);
    }

    String render(final Model model, Map<String, SlotFill> slotFills, final ExpressionResolver parentExpressionResolver) {
        final ExpressionResolver expressionResolver = new ExpressionResolver(model);
        return Renderer.render(renders, new RenderContext(expressionResolver, slotFills, parentExpressionResolver));
    }
}
