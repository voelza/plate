package com.voelza.plate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Model {

    private Map<String, Object> values;

    public Model() {
        this.values = new HashMap<>();
    }

    public void add(final String name, final Object value) {
        this.values.put(name, value);
    }

    public Object get(final String name) {
        return values.get(name);
    }

    public void forEach(BiConsumer<String, Object> forEachFunction) {
        values.forEach(forEachFunction);
    }
}
