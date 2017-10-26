package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.FieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by daichi on 10/26/17.
 */

public class SchemaInfo {

    private String mModelName;
    private String mClassName;
    private Map<String, FieldType> mFieldNamesWithType;

    public SchemaInfo() {
        mFieldNamesWithType = new HashMap<>();
    }

    public String getModelName() {
        return mModelName;
    }

    public String getClassName() {
        return mClassName;
    }

    public FieldType getFieldTypeOf(String fieldName) {
        return mFieldNamesWithType.get(fieldName);
    }

    public Set<String> fieldNameSet() {
        return mFieldNamesWithType.keySet();
    }

    public void setModelName(String modelName) {
        mModelName = modelName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public void addFieldNameAndType(String fieldName, FieldType type) {
        mFieldNamesWithType.put(fieldName, type);
    }
}
