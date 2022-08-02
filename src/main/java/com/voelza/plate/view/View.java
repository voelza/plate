package com.voelza.plate.view;

import com.voelza.plate.Model;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class View {

    private static final String SCOPE_PREFIX = "data-p-";
    private static final String SETUP_PREFIX = "data-p-setup-";

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

    public View(final String path, final Locale locale) {
        this(path, locale, ViewOrigin.ROOT);
    }

    public View(final String path, final Locale locale, final ViewOrigin viewOrigin) {
        this.viewOrigin = viewOrigin;

        final Path filePath = Path.of(path);
        final String fileName = filePath.getFileName().toString();
        final int extensionIndex = fileName.lastIndexOf(".");
        this.name = fileName.substring(0, extensionIndex != -1 ? extensionIndex : fileName.length()).toLowerCase();
        this.directoryPath = filePath.getParent().toString();
        this.locale = locale;

        final Component component = getComponent(path, locale);
        subViews = resolveImports(component.getImports());

        declaredCSS = component.getStyle().orElse("");
        viewCSS = ViewOrigin.ROOT == viewOrigin ? collectCSS(this.name, declaredCSS, subViews) : null;

        declaredJavaScript = component.getScript().orElse("");
        viewJavaScript = ViewOrigin.ROOT == viewOrigin ? collectJavaScript(getDeclaredJavaScript(), subViews) : null;

        props = component.getProps();
        slots = component.getSlots();
        final List<Element> elements = component.getTemplate().map(Element::children).orElse(Collections.emptyList());

        final Attribute scopeAttribute = StringUtils.hasText(this.declaredCSS) ? new Attribute(SCOPE_PREFIX + this.name, null) : null;
        final Attribute setupAttribute =
                StringUtils.hasText(this.declaredJavaScript) ? new Attribute(SETUP_PREFIX + this.name, null) : null;
        renders = RenderCreator.create(
                new RenderCreatorOptions(
                        name,
                        StringUtils.hasText(viewCSS),
                        StringUtils.hasText(viewJavaScript),
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
            subView.put(i.name.toLowerCase(), new View(directoryPath + "/" + i.file, locale, ViewOrigin.COMPONENT));
        }
        return subView;
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
                .collect(Collectors.toMap(Map.Entry::getKey,
                        (e) -> e.getValue().getDeclaredJavaScript()));

        if (!StringUtils.hasText(declaredJavaScript) && jsMap.isEmpty()) {
            return "";
        }

        return String.format("const plate = function() {" +
                        "function setup(selector, setupFunc) {" +
                        "document" +
                        ".querySelectorAll(selector)" +
                        ".forEach(e => {" +
                        "const uuid=e.previousSibling?.textContent;" +
                        "setupFunc({element:e,props:plateModel[uuid]});" +
                        "});" +
                        "}" +
                        "%s" +
                        "}();",
                declaredJavaScript + String.join("", jsMap.values()));
    }

    private String getDeclaredJavaScript() {
        if (!StringUtils.hasText(declaredJavaScript)) {
            return "";
        }

        return String.format("setup('[data-p-setup-%s]',({element}) => {%s});", name, declaredJavaScript);
    }

    public String render(final Model model) {
        return render(model, Collections.emptyMap(), null);
    }

    String render(final Model model, Map<String, SlotFill> slotFills, final ExpressionResolver parentExpressionResolver) {
        final ExpressionResolver expressionResolver = new ExpressionResolver(model);
        final ElementRenderResult renderResult = Renderer.render(
                renders,
                new RenderContext(
                        expressionResolver,
                        slotFills,
                        parentExpressionResolver
                ));
        return addPropScriptIfNeeded(renderResult);
    }

    private String addPropScriptIfNeeded(final ElementRenderResult renderResult) {
        String resultHTML = renderResult.html();
        if (this.viewOrigin == ViewOrigin.ROOT && CollectionUtils.isNotEmpty(renderResult.scriptPropFillsList())) {
            final int bodyEndIndex = resultHTML.indexOf("</body>");
            if (bodyEndIndex == -1) {
                return resultHTML;
            }

            final Map<String, List<ScriptPropFill>> scriptFillProps = new HashMap<>();
            for (final ScriptPropFill scriptPropFill : renderResult.scriptPropFillsList()) {
                List<ScriptPropFill> fills = scriptFillProps.get(scriptPropFill.uuid());
                if (fills == null) {
                    fills = new ArrayList<>();
                }
                fills.add(scriptPropFill);
                scriptFillProps.put(scriptPropFill.uuid(), fills);
            }

            final String propScript = String.format(
                    "<script data-p-props>const plateModel={%s};document.querySelector('script[data-p-props]').remove();</script>",
                    String.join(
                            ",",
                            scriptFillProps.entrySet().stream().map(this::createScriptPropDeclaration).toList()
                    )
            );
            resultHTML = resultHTML.substring(0, bodyEndIndex) + propScript + resultHTML.substring(bodyEndIndex);
        }
        return resultHTML;
    }

    private String createScriptPropDeclaration(final Map.Entry<String, List<ScriptPropFill>> scriptPropsFills) {
        return String.format(
                "\"%s\":{%s}",
                scriptPropsFills.getKey(),
                String.join(",", scriptPropsFills.getValue().stream().map(this::createScriptProp).toList())
        );
    }

    private String createScriptProp(final ScriptPropFill scriptPropFill) {
        return String.format("\"%s\":\"%s\"", scriptPropFill.name(), scriptPropFill.value());
    }

    public String getCSS() {
        return viewCSS;
    }

    public String getJavaScript() {
        return viewJavaScript;
    }
}
