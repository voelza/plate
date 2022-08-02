package com.voelza.plate.view;

import com.voelza.plate.Version;
import com.voelza.plate.html.Element;

import java.util.List;
import java.util.Optional;

class HeadElementRender implements ElementRender {

    private final List<ElementRender> childRenders;

    HeadElementRender(final Element element, final RenderCreatorOptions options) {
        childRenders = RenderCreator.create(options.newElements(element.children()));
        createCSSLink(options).map(StaticElementRender::new).ifPresent(childRenders::add);
        createJSLink(options).map(StaticElementRender::new).ifPresent(childRenders::add);
    }

    private static Optional<String> createJSLink(final RenderCreatorOptions options) {
        if (!options.hasJavaScript()) {
            return Optional.empty();
        }
        return Optional.of(String.format("<script src=\"/plate/js/%s-%s.js\" defer></script>", options.viewName(), Version.get()));
    }

    private static Optional<String> createCSSLink(final RenderCreatorOptions options) {
        if (!options.hasCSS()) {
            return Optional.empty();
        }
        return Optional.of(String.format("<link href=\"/plate/css/%s-%s.css\">", options.viewName(), Version.get()));
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        final ElementRenderResult renderResult = Renderer.render(childRenders, renderContext);
        return new ElementRenderResult("<head>" + renderResult.html() + "</head>", renderResult.scriptPropFillsList());
    }
}
