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

    private static final String NEW_INSTANCE_METHOD = "newInstance";
    private static final String GET_FIELD_NAMES_WITH_TYPE_METHOD = "getFieldNamesWithType";
    private static final String GET_SCHEMA_INFO_METHOD = "getSchemaInfo";

    public static <T extends BaseModel> T newInstanceOf(Class<T> klass) {
        try {
            Method method = klass.getMethod(NEW_INSTANCE_METHOD);
            return klass.cast(method.invoke(null));
        } catch (Exception e) {
            throw new IllegalStateException(klass.getName()
                    + " class does not has " + NEW_INSTANCE_METHOD + "() method");
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, FieldType> getFiledNamesWithTypeOf(Class<? extends BaseModel> klass) {
        try {
            Method method = klass.getMethod(GET_FIELD_NAMES_WITH_TYPE_METHOD);
            return (Map<String, FieldType>) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException(klass.getName()
                    + " class does not has " + GET_FIELD_NAMES_WITH_TYPE_METHOD + "() method");
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
}
