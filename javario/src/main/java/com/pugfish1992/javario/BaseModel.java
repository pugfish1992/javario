package com.pugfish1992.javario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daichi on 10/25/17.
 */

abstract public class BaseModel {

    public static final String FIELD_ID = "id";

    // Anybody can modify this variable except for this class
    private long id;

    public BaseModel() {
        this.id = DataSource.INVALID_ID;
    }

    public long getId() {
        return id;
    }

    /**
     * Get data from db by the id, and initialize fields
     * of this object by fetched data.
     * If failed to fetch data, return false and initialize by default values.
     */
    public boolean fetchById(long id) { return false; }

    public boolean save() { return false; }

    public boolean delete() {
        return false;
    }

    protected static <T extends BaseModel> T findByItemIdFrom(long id, Class<T> klass) {
        return null;
    }

    protected static <T extends BaseModel> List<T> listUpItemsFrom(Class<T> klass) {
        return null;
    }

    protected static <T extends BaseModel> boolean saveItemTo(T item, Class<T> klass) {
        return false;
    }

    protected static <T extends BaseModel> boolean deleteItemFrom(T item, Class<T> klass) {
        return false;
    }

    /**
     * These methods will be generated in annotation processing
     */
    public ValueMap toValueMap() {
        ValueMap valueMap = new ValueMap();
        valueMap.put(FIELD_ID, this.id);
        return valueMap;
    }

    public void initWithValueMap(ValueMap valueMap) {
        this.id = valueMap.getAsLong(FIELD_ID);
    }
}
