package com.pugfish1992.javario;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by daichi on 10/27/17.
 */

class GeneratingModelClassHistoryClassWriter {

    private static final String GENERATED_CLASS_PACKAGE = "com.pugfish1992.javario";
    private static final String CLASS_NAME = "GeneratingModelClassHistory";
    private static final String METHOD_NAME = "getListOfGeneratedModelSchemaInfo";

    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classSchemaInfo = ClassName.get(SchemaInfo.class);
    private static final ClassName classArrayList = ClassName.get(ArrayList.class);

    static void write(Filer filer, Collection<String> generatedClassNames) throws IOException {

        TypeSpec.Builder historyClass = TypeSpec
                .classBuilder(CLASS_NAME)
                .addModifiers(Modifier.FINAL);

        MethodSpec.Builder method = MethodSpec
                .methodBuilder(METHOD_NAME)
                .returns(ParameterizedTypeName.get(classList, classSchemaInfo));

        method.addStatement("$T<$T> list = new $T<>()", classList, classSchemaInfo, classArrayList);
        for (String className : generatedClassNames) {
            // This statement depends on #getSchemaInfo() which is contained in generated model class.
            method.addStatement("list.add($L.getSchemaInfo())", className);
        }
        method.addStatement("return list");

        historyClass.addMethod(method.build());
        JavaFile.builder(GENERATED_CLASS_PACKAGE, historyClass.build()).build().writeTo(filer);
    }
}
