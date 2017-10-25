package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Created by daichi on 10/25/17.
 */

class AnnotationUtils {

    private AnnotationUtils() {}

    static String getModelName(Element element) {
        ModelSchema modelSchema = element.getAnnotation(ModelSchema.class);
        if (modelSchema != null) {
            return modelSchema.value();
        }
        return null;
    }

    static String getSpecifiedFieldNameIfExist(Element element) {
        FieldOption fieldOption = element.getAnnotation(FieldOption.class);
        if (fieldOption != null && 0 < fieldOption.fieldName().length()) {
            return fieldOption.fieldName();
        }
        return null;
    }

    static String getSpecifiedFieldNameConstVariableNameIfExist(Element element) {
        FieldOption fieldOption = element.getAnnotation(FieldOption.class);
        if (fieldOption != null && 0 < fieldOption.constVarName().length()) {
            return fieldOption.constVarName();
        }
        return null;
    }

    static String getSpecifiedPrefixOfFieldNameConstVariableNamesIfExist(Element element) {
        ModelSchemaOption schemaOption = element.getAnnotation(ModelSchemaOption.class);
        if (schemaOption != null) {
            return schemaOption.constVarNamePrefix();
        }
        return null;
    }
}
