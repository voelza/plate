package com.voelza.plate.view;

import com.voelza.plate.Model;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

public class ExpressionResolver {

    private final JexlEngine jexl;
    private final JexlContext jexlContext;

    public ExpressionResolver(final Model model) {
        jexl = new JexlBuilder().create();
        jexlContext = new MapContext();
        model.forEach(jexlContext::set);
    }

    public String evaluate(final String expression) {
        final JexlExpression e = jexl.createExpression(expression);
        return String.valueOf(e.evaluate(jexlContext));
    }
}
