package com.voelza.plate.view;

import com.voelza.plate.Model;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

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

    public String evaluate(final String expression) {
        final JexlExpression e = jexl.createExpression(expression);
        return String.valueOf(e.evaluate(jexlContext));
    }

    public boolean evaluateCondition(final String condition) {
        final JexlExpression e = jexl.createExpression(condition);
        final Object result = e.evaluate(jexlContext);
        if (result instanceof Boolean r) {
            return r;
        }
        throw new IllegalArgumentException(String.format("Condition expression \"%s\" does not result in a boolean value", condition));
    }
}
