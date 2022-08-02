package com.voelza.plate.view;

import com.voelza.plate.Model;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ExpressionResolver {

    private final JexlEngine jexl;
    private final JexlContext jexlContext;

    private final Model model;

    public ExpressionResolver(final Model model) {
        jexl = new JexlBuilder().create();
        jexlContext = new MapContext();

        this.model = model;
        model.forEach(jexlContext::set);
    }

    public static ExpressionResolver merge(final ExpressionResolver resolver1, final ExpressionResolver resolver2) {
        final Model model = new Model();
        Optional.ofNullable(resolver1).ifPresent(r -> r.model.forEach(model::add));
        Optional.ofNullable(resolver2).ifPresent(r -> r.model.forEach(model::add));
        return new ExpressionResolver(model);
    }

    public ExpressionResolver withAdditionalModel(final Model additionalModel) {
        final Model model = new Model();
        this.model.forEach(model::add);
        additionalModel.forEach(model::add);
        return new ExpressionResolver(model);
    }

    public Object evaluate(final String expression) {
        final JexlExpression e = jexl.createExpression(expression);
        return e.evaluate(jexlContext);
    }

    public boolean evaluateCondition(final String condition) {
        final JexlExpression e = jexl.createExpression(condition);
        final Object result = e.evaluate(jexlContext);
        if (result instanceof Boolean r) {
            return r;
        }
        throw new IllegalArgumentException(String.format("Condition expression \"%s\" does not result in a boolean value", condition));
    }

    public Collection<?> evaluateCollection(final String expression) {
        final JexlExpression e = jexl.createExpression(expression);
        final Object result = e.evaluate(jexlContext);
        if (result instanceof Collection<?> collection) {
            return collection;
        }

        if (result instanceof Array array) {
            return List.of(array);
        }
        throw new IllegalArgumentException(String.format("Collection expression \"%s\" does not result in a collection value", expression));
    }
}
