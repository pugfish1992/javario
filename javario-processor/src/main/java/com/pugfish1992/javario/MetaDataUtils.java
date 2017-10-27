package com.pugfish1992.javario;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

/**
 * Created by daichi on 10/25/17.
 */

class MetaDataUtils {

    private static final ClassName stringClass = ClassName.get(String.class);

    private MetaDataUtils() {}

    static TypeName getVariableType(VariableElement element) {
        return TypeName.get(element.asType());
    }

    static String getVariableName(VariableElement element) {
        return element.getSimpleName().toString();
    }

    /**
     * In this library, int, long, boolean, String are supported.
     */
    static boolean isSupportedType(TypeName typeName) {
        return typeName.equals(TypeName.INT) ||
                typeName.equals(TypeName.LONG) ||
                typeName.equals(TypeName.BOOLEAN) ||
                typeName.equals(stringClass);
    }

    static boolean isIntType(TypeName typeName) {
        return TypeName.INT.equals(typeName);
    }

    static boolean isLongType(TypeName typeName) {
        return TypeName.LONG.equals(typeName);
    }

    static boolean isBooleanType(TypeName typeName) {
        return TypeName.BOOLEAN.equals(typeName);
    }

    static boolean isStringType(TypeName typeName) {
        return typeName.equals(stringClass);
    }
}
