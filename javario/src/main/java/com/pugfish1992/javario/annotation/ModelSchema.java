package com.pugfish1992.javario.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ModelSchema {
    // Model name
    String value();
    // name of a model class which will be generated (optional)
    String className() default "";
}
