package com.voelza.plate.view;

public record RenderContext(
        String viewName,
        ExpressionResolver expressionResolver,
        SlotFills slotFills,
        ExpressionResolver parentExpressionResolver) {
}
