package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.ModelSchema;

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
}
