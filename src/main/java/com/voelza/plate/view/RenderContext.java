package com.voelza.plate.view;

import java.util.Map;

public record RenderContext(ExpressionResolver expressionResolver,
                            Map<String, SlotFill> slotFills,
                            ExpressionResolver parentExpressionResolver) {
}
