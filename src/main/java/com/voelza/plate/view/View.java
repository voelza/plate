package com.voelza.plate.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voelza.plate.Model;
import com.voelza.plate.Plate;
import com.voelza.plate.ViewKeyCreator;
import com.voelza.plate.component.Component;
import com.voelza.plate.component.ComponentResolver;
import com.voelza.plate.component.Import;
import com.voelza.plate.component.Prop;
import com.voelza.plate.component.Slot;
import com.voelza.plate.css.CSSParser;
import com.voelza.plate.html.Attribute;
import com.voelza.plate.html.Element;
import com.voelza.plate.utils.CollectionUtils;
import com.voelza.plate.utils.StringUtils;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class View {

    private static final String SCOPE_PREFIX = "data-p-";
    private static final String SETUP_PREFIX = "data-p-setup-";
    private static final ObjectMapper JSON_PARSER = new ObjectMapper();

    private final String rootKey;
    private final ViewOrigin viewOrigin;
    private final String name;
    private final String directoryPath;
    private final Locale locale;
    final List<ElementRender> renders;
    final Map<String, View> subViews;

    final List<Prop> props;
    final List<Slot> slots;

    private final String declaredCSS;
    private final String viewCSS;

    private final String declaredJavaScript;
    private final String viewJavaScript;

    private final AtomicBoolean hasCSS;
    private final AtomicBoolean hasJavaScript;

    public View(final String path, final Locale locale) {
        this(path, path, locale, ViewOrigin.ROOT, new AtomicBoolean(false), new AtomicBoolean(false));
    }

    public View(
            final String path,
            final String rootKey,
            final Locale locale,
            final ViewOrigin viewOrigin,
            final AtomicBoolean hasCSS,
            final AtomicBoolean hasJavaScript
    ) {
        this.viewOrigin = viewOrigin;

        final String viewKey = ViewKeyCreator.create(path);
        if (ViewOrigin.ROOT == this.viewOrigin) {
            this.rootKey = viewKey;
        } else {
            this.rootKey = rootKey;
        }

        final Path filePath = Path.of(path);
        final String fileName = filePath.getFileName().toString();
        final int extensionIndex = fileName.lastIndexOf(".");
        this.name = fileName.substring(0, extensionIndex != -1 ? extensionIndex : fileName.length()).toLowerCase();
        this.directoryPath = Optional.ofNullable(filePath.getParent()).map(Path::toString).orElse("");
        this.locale = locale;
        this.hasCSS = hasCSS;
        this.hasJavaScript = hasJavaScript;

        final Component component = getComponent(path, locale);
        subViews = resolveImports(component.getImports());

        declaredCSS = component.getStyle().orElse("");
        viewCSS = ViewOrigin.ROOT == viewOrigin ? collectCSS(this.name, declaredCSS, subViews) : null;

        declaredJavaScript = component.getScript().orElse("");
        viewJavaScript = ViewOrigin.ROOT == viewOrigin ? collectJavaScript(getDeclaredJavaScript(), subViews) : null;

        if (ViewOrigin.ROOT == viewOrigin) {
            this.hasCSS.set(StringUtils.hasText(viewCSS));
            this.hasJavaScript.set(StringUtils.hasText(viewJavaScript));
        }

        props = component.getProps();
        slots = component.getSlots();
        final List<Element> elements = component.getTemplate().map(Element::children).orElse(Collections.emptyList());

        final Attribute scopeAttribute = StringUtils.hasText(this.declaredCSS) ? new Attribute(SCOPE_PREFIX + this.name, null) : null;
        final Attribute setupAttribute =
                StringUtils.hasText(this.declaredJavaScript) ? new Attribute(SETUP_PREFIX + this.name, null) : null;
        renders = RenderCreator.create(
                new RenderCreatorOptions(
                        viewKey,
                        this.rootKey,
                        this.hasCSS,
                        this.hasJavaScript,
                        elements,
                        subViews,
                        scopeAttribute,
                        setupAttribute
                )
        );
    }

    private static Component getComponent(final String path, final Locale locale) {
        return ComponentResolver.resolve(path, locale)
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve component " + path));
    }

    private Map<String, View> resolveImports(final List<Import> imports) {
        final Map<String, View> subView = new HashMap<>();
        for (final Import i : imports) {
            subView.put(i.name.toLowerCase(), new View(
                    getImportPath(i),
                    this.rootKey,
                    locale,
                    ViewOrigin.COMPONENT,
                    this.hasCSS,
                    this.hasJavaScript
            ));
        }
        return subView;
    }

    private String getImportPath(final Import i) {
        final String importFilePath = i.file;
        if (importFilePath.startsWith("@")) {
            return Plate.getTemplatesPath() + importFilePath.substring(1);
        }
        return getDirectoryPath() + i.file;
    }

    private String getDirectoryPath() {
        return Optional
                .ofNullable(directoryPath)
                .filter(StringUtils::hasText)
                .map(d -> d + "/")
                .orElse("");
    }

    private static String collectCSS(final String viewName, final String declaredCSS, final Map<String, View> subViews) {
        final Map<String, String> cssMap = subViews
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        (e) -> CSSParser.scopeCSS(e.getValue().declaredCSS, SCOPE_PREFIX + e.getValue().name)));
        return CSSParser.scopeCSS(declaredCSS, SCOPE_PREFIX + viewName) + String.join("", cssMap.values());
    }

    private static String collectJavaScript(final String declaredJavaScript, final Map<String, View> subViews) {
        final Map<String, String> jsMap = subViews
                .entrySet()
                .stream()
                .filter(e -> StringUtils.hasText(e.getValue().getDeclaredJavaScript()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        (e) -> e.getValue().getDeclaredJavaScript()));

        if (!StringUtils.hasText(declaredJavaScript) && jsMap.isEmpty()) {
            return "";
        }

        return String.format("if(!plateModel){var plateModel = {};}" +
                        "(() => {" +
                        "function setup(selector, setupFunc) {" +
                        "document" +
                        ".querySelectorAll(selector)" +
                        ".forEach(e => {" +
                        "const uuid=e.previousSibling?.textContent ?? 'main';" +
                        "setupFunc({element:e,props:plateModel[uuid]});" +
                        "});" +
                        "}" +
                        "%s" +
                        "})();",
                declaredJavaScript + String.join("", jsMap.values()));
    }

    private String getDeclaredJavaScript() {
        if (!StringUtils.hasText(declaredJavaScript)) {
            return "";
        }

        return String.format("setup('[data-p-setup-%s]',({element,props}) => {%s});", name, declaredJavaScript);
    }

    public String render(final Model model) {
        return render(null, model, SlotFills.empty(), null);
    }

    String render(
            final String uuid,
            final Model model,
            final SlotFills slotFills,
            final ExpressionResolver parentExpressionResolver
    ) {
        final ExpressionResolver expressionResolver = new ExpressionResolver(model);
        final ElementRenderResult renderResult = Renderer.render(
                uuid != null ? addUUIDRenders(uuid, renders) : renders,
                new RenderContext(
                        this.name,
                        expressionResolver,
                        slotFills,
                        parentExpressionResolver
                ));
        return getPropScript(renderResult.scriptPropFillsList(), expressionResolver)
                .map(propScript -> renderResult.html() + propScript)
                .orElse(renderResult.html());
    }

    public void stream(final PrintWriter printWriter, final Model model) {
        stream(printWriter, null, model, SlotFills.empty(), null);
    }

    void stream(
            final PrintWriter printWriter,
            final String uuid,
            final Model model,
            final SlotFills slotFills,
            final ExpressionResolver parentExpressionResolver
    ) {
        final ExpressionResolver expressionResolver = new ExpressionResolver(model);
        final ElementStreamResult streamResult = Renderer.stream(
                printWriter,
                uuid != null ? addUUIDRenders(uuid, renders) : renders,
                new RenderContext(
                        this.name,
                        expressionResolver,
                        slotFills,
                        parentExpressionResolver
                ));
        getPropScript(streamResult.scriptPropFillsList(), expressionResolver).ifPresent(printWriter::print);
    }

    private List<ElementRender> addUUIDRenders(final String uuid, final List<ElementRender> renders) {
        final UUIDElementRender uuidElementRender = new UUIDElementRender(uuid);
        final List<ElementRender> newRenders = new ArrayList<>();
        for (final ElementRender render : renders) {
            newRenders.add(uuidElementRender);
            newRenders.add(render);
        }
        return Collections.unmodifiableList(newRenders);
    }

    private Optional<String> getPropScript(final List<ScriptPropFill> scriptPropFills, final ExpressionResolver expressionResolver) {
        if (this.viewOrigin != ViewOrigin.ROOT || (CollectionUtils.isEmpty(scriptPropFills) && CollectionUtils.isEmpty(this.props))) {
            return Optional.empty();
        }

        final Map<String, List<ScriptPropFill>> scriptFillProps = new HashMap<>();
        scriptFillProps.put(
                "main",
                this.props
                        .stream()
                        .filter(p -> p.inScript).map(p -> new ScriptPropFill("main", p.name, expressionResolver.evaluate(p.name)))
                        .toList());

        for (final ScriptPropFill scriptPropFill : scriptPropFills) {
            List<ScriptPropFill> fills = scriptFillProps.get(scriptPropFill.uuid());
            if (fills == null) {
                fills = new ArrayList<>();
            }
            fills.add(scriptPropFill);
            scriptFillProps.put(scriptPropFill.uuid(), fills);
        }

        return Optional.of(
                String.format(
                        "<script data-p-props>var plateModel={%s};document.querySelector('script[data-p-props]').remove();</script>",
                        String.join(
                                ",",
                                scriptFillProps.entrySet().stream().map(this::createScriptPropDeclaration).toList()
                        )
                ));
    }

    private String createScriptPropDeclaration(final Map.Entry<String, List<ScriptPropFill>> scriptPropsFills) {
        return String.format(
                "\"%s\":{%s}",
                scriptPropsFills.getKey(),
                String.join(",", scriptPropsFills.getValue().stream().map(this::createScriptProp).toList())
        );
    }

    private String createScriptProp(final ScriptPropFill scriptPropFill) {
        return String.format("\"%s\":%s", scriptPropFill.name(), getJavaScriptValue(scriptPropFill.value()));
    }

    private String getJavaScriptValue(final Object value) {
        try {
            return JSON_PARSER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCSS() {
        return viewCSS;
    }

    public String getJavaScript() {
        return viewJavaScript;
    }
}
