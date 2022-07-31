package com.voelza.plate.view;

import java.util.Map;

public record RenderOptions(ExpressionResolver expressionResolver, Map<String, SlotFill> slotFills) {
}
