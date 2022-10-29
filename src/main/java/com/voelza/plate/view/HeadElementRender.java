package com.voelza.plate.view;

import com.voelza.plate.Version;
import com.voelza.plate.html.Element;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

class HeadElementRender implements ElementRender {

    private final List<ElementRender> childRenders;
    private final RenderCreatorOptions options;
    private boolean initialized;

    HeadElementRender(final Element element, final RenderCreatorOptions options) {
        childRenders = RenderCreator.create(options.newElements(element.children()));
        this.options = options;
        initialized = false;
    }

    private static Optional<String> createJSLink(final RenderCreatorOptions options) {
        if (!options.hasJavaScript().get()) {
            return Optional.empty();
        }
        return Optional.of(String.format("<script src=\"/plate/js/%s-%s.js\" defer></script>", options.rootKey(), Version.get()));
    }

    private static Optional<String> createCSSLink(final RenderCreatorOptions options) {
        if (!options.hasCSS().get()) {
            return Optional.empty();
        }
        return Optional.of(String.format("<link rel=\"stylesheet\" href=\"/plate/css/%s-%s.css\">", options.rootKey(), Version.get()));
    }

    private void initialize() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        createCSSLink(options).map(StaticElementRender::new).ifPresent(childRenders::add);
        createJSLink(options).map(StaticElementRender::new).ifPresent(childRenders::add);
    }

    @Override
    public ElementRenderResult renderHTML(final RenderContext renderContext) {
        initialize();
        final ElementRenderResult renderResult = Renderer.render(childRenders, renderContext);
        return new ElementRenderResult("<head>" + renderResult.html() + "</head>", renderResult.scriptPropFillsList());
    }

    @Override
    public ElementStreamResult stream(final PrintWriter printWriter, final RenderContext renderContext) {
        initialize();
        printWriter.print("<head>");
        printWriter.flush();
        final ElementStreamResult result = Renderer.stream(printWriter, childRenders, renderContext);
        printWriter.print("</head>");
        printWriter.flush();
        return result;
    }
}
