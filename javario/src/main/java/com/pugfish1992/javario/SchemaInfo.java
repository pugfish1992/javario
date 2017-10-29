package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.FieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by daichi on 10/26/17.
 */

public final class SchemaInfo {

    private String mModelName;
    private String mClassName;
    private String mNameOfPrimaryKeyField;
    private Map<String, FieldType> mFieldNamesWithType;

    SchemaInfo() {
        mFieldNamesWithType = new HashMap<>();
    }

    public String getModelName() {
        return mModelName;
    }

    public String getClassName() {
        return mClassName;
    }

    public String getNameOfPrimaryKeyField() {
        return mNameOfPrimaryKeyField;
    }

    public FieldType getFieldTypeOf(String fieldName) {
        return mFieldNamesWithType.get(fieldName);
    }

    public Set<String> fieldNameSet() {
        return mFieldNamesWithType.keySet();
    }

    public Set<String> fieldNameSetWithoutPrimaryKey() {
        Set<String> set = mFieldNamesWithType.keySet();
        set.remove(mNameOfPrimaryKeyField);
        return set;
    }

    /* Intentional package-private */
    void setModelName(String modelName) {
        mModelName = modelName;
    }

    /* Intentional package-private */
    void setClassName(String className) {
        mClassName = className;
    }

    /* Intentional package-private */
    void setNameOfPrimaryKeyField(String nameOfPrimaryKeyField) {
        mNameOfPrimaryKeyField = nameOfPrimaryKeyField;
    }

    /* Intentional package-private */
    void addFieldNameAndType(String fieldName, FieldType type) {
        mFieldNamesWithType.put(fieldName, type);
    }
}
