package com.voelza.plate.view;

import java.util.Collections;
import java.util.Map;

class SlotFills {

    private final Map<String, SlotFill> slotFills;
    private final SlotFills parentFills;

    SlotFills(final Map<String, SlotFill> slotFills) {
        this(slotFills, null);
    }

    SlotFills(final Map<String, SlotFill> slotFills, final SlotFills parentFills) {
        this.slotFills = slotFills;
        this.parentFills = parentFills;
    }

    static SlotFills empty() {
        return new SlotFills(Collections.emptyMap());
    }

    SlotFill get(final String name) {
        final SlotFill slotFill = slotFills.get(name);
        if (slotFill != null) {
            return slotFill;
        }
        if (parentFills != null) {
            return parentFills.get(name);
        }
        return null;
    }

    SlotFills getParentFills() {
        return parentFills;
    }
}
