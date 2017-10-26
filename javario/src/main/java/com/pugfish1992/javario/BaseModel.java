package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;
import com.pugfish1992.javario.datasource.ValueMap;

import java.util.List;

/**
 * Created by daichi on 10/25/17.
 */

abstract public class BaseModel {

    public static final String FIELD_ID = "mId";

    // Anybody can modify this variable except for this class
    private long mId;

    public BaseModel() {
        this.mId = DataSource.INVALID_ID;
    }

    public long getId() {
        return mId;
    }

    /**
     * Get data from db by the mId, and initialize fields
     * of this object by fetched data.
     * If failed to fetch data, return false.
     */
    public boolean fetchById(long id) {
        BaseModel model = findByItemIdFrom(id, this.getClass());
        if (model == null) return false;
        initWithValueMap(model.toValueMap());
        return true;
    }

    public boolean save() {
        return saveItemTo(this);
    }

    public boolean delete() {
        return deleteItemFrom(this);
    }

    /**
     * The following static method will be used ONLY by generated model classes.
     * ---------- */

    static <T extends BaseModel> T findByItemIdFrom(long id, Class<? extends BaseModel> klass) {
        return Repository.api().findItemByIdFrom(id, klass);
    }

    static <T extends BaseModel> List<T> listItemsFrom(Class<? extends BaseModel> klass) {
        return Repository.api().listItemsFrom(klass);
    }

    static <T extends BaseModel> boolean saveItemTo(T item) {
        return Repository.api().saveItemTo(item);
    }

    static <T extends BaseModel> boolean deleteItemFrom(T item) {
        return Repository.api().deleteItemFrom(item);
    }

    /**
     * These methods will be generated in annotation processing
     */
    public ValueMap toValueMap() {
        ValueMap valueMap = new ValueMap();
        valueMap.put(FIELD_ID, this.mId);
        return valueMap;
    }

    public void initWithValueMap(ValueMap valueMap) {
        this.mId = valueMap.getAsLong(FIELD_ID);
    }
}
