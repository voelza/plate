package com.voelza.plate.view;

import com.voelza.plate.Model;
import org.apache.commons.jexl3.JexlContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class ModelContext implements JexlContext {

    private final Map<String, Object> values;

    ModelContext(final Model model) {
        this.values = new HashMap<>();
        model.forEach(this.values::put);
    }

    @Override
    public Object get(final String s) {
        Object value = this.values.get(s);
        if (value instanceof Supplier<?> supplier) {
            value = supplier.get();
            this.values.put(s, value);
        }
        return value;
    }

    @Override
    public void set(final String s, final Object o) {
        this.values.put(s, o);
    }

    @Override
    public boolean has(final String s) {
        return this.values.containsKey(s);
    }
}
