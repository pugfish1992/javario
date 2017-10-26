package com.pugfish1992.javario.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daichi on 10/25/17.
 */

public class ValueMap {

    // Key is a field name, value is ∂ata
    private Map<String, Object> mFieldNamesWithData;

    public ValueMap() {
        mFieldNamesWithData = new HashMap<>();
    }

    public ValueMap(ValueMap valueMap) {
        mFieldNamesWithData = new HashMap<>(valueMap.mFieldNamesWithData);
    }

    /**
     * PUT
     *
     * Supported types are int, long, boolean, String.
     * ---------- */

    private ValueMap generalPut(String fieldName, Object data) {
        if (fieldName == null) {
            throw new NullPointerException("fieldName cannot be a null");
        }

        mFieldNamesWithData.put(fieldName, data);
        return this;
    }

    public ValueMap put(String fieldName, int data) {
        return generalPut(fieldName, data);
    }

    public ValueMap put(String fieldName, long data) {
        return generalPut(fieldName, data);
    }

    public ValueMap put(String fieldName, boolean data) {
        return generalPut(fieldName, data);
    }

    public ValueMap put(String fieldName, String data) {
        return generalPut(fieldName, data);
    }

    /**
     * GET
     * ---------- */

    private <T> T generalGet(String fieldName, Class<T> klass) {
        if (fieldName == null) {
            throw new NullPointerException("fieldName cannot be a null");
        }
        if (mFieldNamesWithData.containsKey(fieldName)) {
            return klass.cast(mFieldNamesWithData.get(fieldName));
        }
        return null;
    }

    public int getAsInt(String fieldName) {
        Integer data = generalGet(fieldName, Integer.class);
        return (data != null) ? data : 0;
    }

    public long getAsLong(String fieldName) {
        Long data = generalGet(fieldName, Long.class);
        return (data != null) ? data : 0L;
    }

    public boolean getAsBoolean(String fieldName) {
        Boolean data = generalGet(fieldName, Boolean.class);
        return (data != null) ? data : false;
    }

    public String getAsString(String fieldName) {
        String data = generalGet(fieldName, String.class);
        return (data != null) ? data : null;
    }

    /**
     * UTILITY
     * ---------- */

    public void clear() {
        mFieldNamesWithData.clear();
    }

    public void remove(String fieldName) {
        mFieldNamesWithData.remove(fieldName);
    }

    public boolean contains(String fieldName) {
        return mFieldNamesWithData.containsKey(fieldName);
    }
}
