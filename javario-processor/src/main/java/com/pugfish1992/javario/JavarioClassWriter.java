package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/*

// This class will generates a Javario class such as below:

public final class Javario {
  public static void initialize(DataSource<BaseModel> rootStorage) {
    List<SchemaInfo> schemaInfoList = new ArrayList<>();
    schemaInfoList.add(Luigi.getSchemaInfo());
    schemaInfoList.add(Mario.getSchemaInfo());
    if (rootStorage != null) {
      rootStorage.onInitialize(Collections.unmodifiableList(schemaInfoList));
    }
    Repository.initialize(rootStorage);
  }
}

 */

class JavarioClassWriter {

    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classSchemaInfo = ClassName.get(SchemaInfo.class);
    private static final ClassName classArrayList = ClassName.get(ArrayList.class);
    private static final ClassName classDataSource = ClassName.get(DataSource.class);
    private static final ClassName classBaseModel = ClassName.get(BaseModel.class);
    private static final ClassName classCollections = ClassName.get(Collections.class);
    private static final ClassName classRepository = ClassName.get(Repository.class);

    static void write(Filer filer, String packageName,
                      Collection<String> generatedModelClassNames) throws IOException {

        TypeSpec.Builder javarioClass = TypeSpec
                .classBuilder("Javario")
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        final String firstParamName = "rootStorage";
        ParameterSpec firstParam = ParameterSpec.builder(
                ParameterizedTypeName.get(classDataSource, classBaseModel),
                firstParamName)
                .build();

        MethodSpec.Builder initializeMethod = MethodSpec
                .methodBuilder("initialize")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(firstParam);

        // --- Begin of coding ------------------------------------------------------------

        initializeMethod.addStatement("$T<$T> schemaInfoList = new $T<>()", classList, classSchemaInfo, classArrayList);
        for (String className : generatedModelClassNames) {
            // This statement depends on #getSchemaInfo() which is defined in generated model class.
            initializeMethod.addStatement("schemaInfoList.add($L.getSchemaInfo())", className);
        }

        initializeMethod
                .beginControlFlow("if ($L != null)", firstParamName)
                .addStatement("$L.onInitialize($T.unmodifiableList(schemaInfoList))", firstParamName, classCollections)
                .endControlFlow()
                .addStatement("$T.initialize($L)", classRepository, firstParamName);

        // --- End of coding --------------------------------------------------------------

        javarioClass.addMethod(initializeMethod.build());
        JavaFile.builder(packageName, javarioClass.build())
                .build().writeTo(filer);
    }
}
