package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;

import java.util.AbstractMap;
import java.util.Map;

import javax.lang.model.element.Element;

/**
 * Created by daichi on 10/25/17.
 */

class AnnotationUtils {

    private AnnotationUtils() {}

    /**
     *
     * @return The key is a model name, and the value is a model class name.
     */
    static Map.Entry<String, String> getModelNameAndClassName(Element element) {
        ModelSchema modelSchema = element.getAnnotation(ModelSchema.class);
        if (modelSchema != null) {
            String modelName = modelSchema.value();
            String className = modelSchema.className();
            if (className.length() == 0) {
                className = null;
            }

            return new AbstractMap.SimpleEntry<String, String>(modelName, className);
        }

        return new AbstractMap.SimpleEntry<String, String>(null, null);
    }

    static String getSpecifiedFieldNameIfExist(Element element) {
        FieldOption fieldOption = element.getAnnotation(FieldOption.class);
        // Do not allow an empty name
        if (fieldOption != null && 0 < fieldOption.fieldName().length()) {
            return fieldOption.fieldName();
        }
        return null;
    }

    static String getSpecifiedFieldNameConstVariableNameIfExist(Element element) {
        FieldOption fieldOption = element.getAnnotation(FieldOption.class);
        // Do not allow an empty name
        if (fieldOption != null && 0 < fieldOption.constStringName().length()) {
            return fieldOption.constStringName();
        }
        return null;
    }

    static String getSpecifiedPrefixOfFieldNameConstVariableNamesIfExist(Element element) {
        ModelSchemaOption schemaOption = element.getAnnotation(ModelSchemaOption.class);
        // Allow an empty prefix
        if (schemaOption != null) {
            return schemaOption.constStringNamePrefix();
        }
        return null;
    }
}
