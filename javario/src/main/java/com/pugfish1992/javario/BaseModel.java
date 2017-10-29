package com.pugfish1992.javario;

import android.content.ContentValues;

import java.util.List;

/**
 * Created by daichi on 10/25/17.
 */

abstract public class BaseModel {

    /**
     * Get data from db by the primary key, and initialize fields
     * of this object by fetched data.
     * If failed to fetch data, return false.
     */
    public boolean fetch(long primaryKey) {
        BaseModel model = findItemFrom(primaryKey, this.getClass());
        if (model == null) return false;
        initWithValueMap(model.toValueMap());
        return true;
    }

    public boolean save() {
        return saveItem(this);
    }

    public boolean delete() {
        return deleteItem(this);
    }

    /**
     * The following static method will be used ONLY by generated model classes.
     * ---------- */

    /* Intentional package-private */
    static <T extends BaseModel> T findItemFrom(long primaryKey, Class<? extends BaseModel> klass) {
        return Repository.api().findItemFrom(primaryKey, klass);
    }

    /* Intentional package-private */
    static <T extends BaseModel> List<T> listItemsFrom(Class<? extends BaseModel> klass) {
        return Repository.api().listItemsFrom(klass);
    }

    /* Intentional package-private */
    static <T extends BaseModel> boolean saveItem(T item) {
        return Repository.api().saveItem(item);
    }

    /* Intentional package-private */
    static <T extends BaseModel> boolean deleteItem(T item) {
        return Repository.api().deleteItem(item);
    }

    /**
     * These methods will be generated in annotation processing
     */

    abstract public long getPrimaryKey();
    abstract public void setPrimaryKey(long primaryKey);
    abstract public ContentValues toValueMap();
    abstract public void initWithValueMap(ContentValues valueMap);
}
