package com.pugfish1992.javario.datasource;

import com.pugfish1992.javario.BaseModel;
import com.pugfish1992.javario.SchemaInfo;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by daichi on 10/25/17.
 */

public class ModelUtils {

    private ModelUtils() {}

    private static final String GET_SCHEMA_INFO_METHOD = "getSchemaInfo";
    private static final String GETTING_MODEL_NAME_METHOD = "modelName";

    public static <T extends BaseModel> T newInstanceOf(Class<T> klass) {
        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(klass.getName()
                    + " class does not has public & emply default constructor method");
        }
    }

    public static SchemaInfo getSchemaInfoOf(Class<? extends BaseModel> klass) {
        try {
            Method method = klass.getMethod(GET_SCHEMA_INFO_METHOD);
            return (SchemaInfo) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException(klass.getName()
                    + " class does not has " + GET_SCHEMA_INFO_METHOD + "() method");
        }
    }

    public static String getModelNameOf(Class<? extends BaseModel> klass) {
        try {
            Method method = klass.getMethod(GETTING_MODEL_NAME_METHOD);
            return (String) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException(klass.getName()
                    + " class does not has " + GETTING_MODEL_NAME_METHOD + "() method");
        }
    }
}
