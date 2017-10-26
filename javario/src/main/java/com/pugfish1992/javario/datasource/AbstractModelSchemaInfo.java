package com.pugfish1992.javario.datasource;

import com.pugfish1992.javario.BaseModel;
import com.pugfish1992.javario.SchemaInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daichi on 10/26/17.
 */

public abstract class AbstractModelSchemaInfo {

    private Map<Class<? extends BaseModel>, Info> mModelClassesWithInfo;

    /* Intentional package-private */
    abstract Map<Class<? extends BaseModel>, String> getModelClassesWithModelName();

    public AbstractModelSchemaInfo() {
        mModelClassesWithInfo = new HashMap<>();
        for (Map.Entry<Class<? extends BaseModel>, String> entry : getModelClassesWithModelName().entrySet()) {
            Info info = new Info();
            info.fieldNamesWithType = ModelUtils.getFiledNamesWithTypeOf(entry.getKey());
            info.modelName = entry.getValue();
        }
    }

    public String getModelNameOf(Class<? extends BaseModel> modelClass) {
        Info info = mModelClassesWithInfo.get(modelClass);
        if (info == null) return null;
        return info.modelName;
    }

    /*
    TODO; REMOVE THIS METHOD
     */
    Map<Class<?>, String> exapmleGetModelClassesWithModelName() {
        Map<Class<?>, String> map = new HashMap<>();
        map.put(String.class, "stringModel");
        map.put(Integer.class, "intModel");
        return map;
    }

    /* ------------------------------------------------------------------------------- *
     * DATA HOLDER CLASS
     * ------------------------------------------------------------------------------- */

    /* Intentional private */
    private static final class Info {
        String modelName;
        Map<String, FieldType> fieldNamesWithType;
    }

    static Map<String, FieldType> getFieldNamesWithType() {
        SchemaInfo info = new SchemaInfo();
        info.setClassName("");
        info.setModelName("");
        info.addFieldNameAndType("", null);

        Map<String, FieldType> map = new HashMap<>();
//        map.put(FIELD_ID, FieldType.LONG_TYPE);
//        map.put(FIELD_LIFE_COUNT, FieldType.INT_TYPE);
//        map.put(FIELD_WITH_LUIGI, FieldType.BOOLEAN_TYPE);
//        map.put(FIELD_KILL_COUNT, FieldType.LONG_TYPE);
//        map.put(FIELD_SHOUT, FieldType.STRING_TYPE);
        return map;
    }
}
