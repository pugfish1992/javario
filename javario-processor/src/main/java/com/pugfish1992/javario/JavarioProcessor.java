package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.ModelSchema;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.pugfish1992.javario.annotation.ModelSchema")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JavarioProcessor extends AbstractProcessor {

    private static final String GENERATED_CLASS_PACKAGE = BaseModel.class.getPackage().getName();

    private static final ClassName classString = ClassName.get(String.class);
    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classMap = ClassName.get(Map.class);
    private static final ClassName classFieldType = ClassName.get(FieldType.class);

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(ModelSchema.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            try {
                writeModelClass((TypeElement) element);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void writeModelClass(TypeElement typeElement) throws IOException {
        ClassName modelClassName = ClassName.get(
                GENERATED_CLASS_PACKAGE, AnnotationUtils.getModelName(typeElement));

        TypeSpec.Builder modelClass = TypeSpec
                .classBuilder(modelClassName.simpleName())
                .superclass(BaseModel.class)
                .addModifiers(Modifier.PUBLIC);

        final String FIELD_NAME_CONSTANT_PREFIX = "FIELD_";
        // Key is a fieldName
        Map<String, String> fieldNamesWithFieldNameConstantNames = new HashMap<>();
        Map<String, String> fieldNamesWithFieldVariableNames = new HashMap<>();
        Map<String, TypeName> fieldNamesWithTypes = new HashMap<>();

        // Field name constants & fields
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                String fieldName = MetaDataUtils.getFieldName(variableElement);
                TypeName typeName = MetaDataUtils.getFieldType(variableElement);

                if (!MetaDataUtils.isTypeSupported(typeName)) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR,
                            typeName.toString() + " type does not supported.");
                }

                fieldNamesWithTypes.put(fieldName, typeName);
                fieldNamesWithFieldVariableNames.put(fieldName, fieldName);
                fieldNamesWithFieldNameConstantNames.put(fieldName,
                        FIELD_NAME_CONSTANT_PREFIX + StringUtils.camelToCapitalSnake(fieldName));
            }
        }

        // Variables
        for (String fieldName : fieldNamesWithTypes.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableNames.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantNames.get(fieldName);
            TypeName fieldType = fieldNamesWithTypes.get(fieldName);

            modelClass.addField(buildField(fieldValName, fieldType));
            modelClass.addField(buildFieldNameConstant(fieldName, fieldConstName));
        }

        // Methods for CRUD
        modelClass.addMethod(buildFindItemByIdMethod(modelClassName));
        modelClass.addMethod(buildListUpItemsMethod(modelClassName));
        modelClass.addMethod(buildSaveItemMethod(modelClassName));
        modelClass.addMethod(buildDeleteItemMethod(modelClassName));

        // Utility methods for DataSource
        modelClass.addMethod(buildToValueMapMethod(
                fieldNamesWithFieldNameConstantNames, fieldNamesWithFieldVariableNames));
        modelClass.addMethod(buildInitWithValueMapMethod(
                fieldNamesWithFieldNameConstantNames, fieldNamesWithFieldVariableNames, fieldNamesWithTypes));
        modelClass.addMethod(buildNewInstanceMethod(modelClassName));
        modelClass.addMethod(buildGetFieldNamesAndTypesMethod(
                fieldNamesWithFieldNameConstantNames, fieldNamesWithTypes));

        JavaFile.builder(GENERATED_CLASS_PACKAGE, modelClass.build()).build().writeTo(mFiler);
    }

    /**
     * For example, #buildFieldNameConstant("userName") will generates:
     *
     * > public static final String FIELD_USER_NAME = "userName";
     *
     */
    private FieldSpec buildFieldNameConstant(String fieldName, String constVariableName) {
        return FieldSpec.builder(String.class,
                constVariableName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", fieldName)
                .build();
    }

    /**
     * For example, #buildField("age", TypeName.INT) will generates:
     *
     * > public int age;
     *
     */
    private FieldSpec buildField(String variableName, TypeName type) {
        return FieldSpec.builder(type, variableName, Modifier.PUBLIC).build();
    }

    /**
     * For example, #buildFindItemByIdMethod(ClassName.get(XXX.class)) will generates:
     *
     * > public static XXX findItemById(long id) {
     * >     return BaseModel.findByItemIdFrom(id, XXX.class);
     * > }
     */
    private MethodSpec buildFindItemByIdMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(TypeName.LONG, "id").build();
        return MethodSpec
                .methodBuilder("findItemById")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(modelClass)
                .addStatement("return $T.findByItemIdFrom(id, $L.class)", BaseModel.class, modelClass.simpleName())
                .build();
    }

    /**
     * For example, #buildListUpItemsMethod(ClassName.get(XXX.class)) will generates:
     *
     * > public static List<\XXX\> listUpItems() {
     * >     return BaseModel.listUpItemsFrom(id, XXX.class);
     * > }
     */
    private MethodSpec buildListUpItemsMethod(ClassName modelClass) {
        return MethodSpec
                .methodBuilder("listUpItems")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(classList, modelClass))
                .addStatement("return $T.listUpItemsFrom($L.class)", BaseModel.class, modelClass.simpleName())
                .build();
    }

    /**
     * For example, #buildSaveItemMethod(ClassName.get(XXX.class)) will generates:
     *
     * > public static boolean saveItem(XXX item) {
     * >     return BaseModel.saveItemTo(item, XXX.class);
     * > }
     */
    private MethodSpec buildSaveItemMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(modelClass, "item").build();
        return MethodSpec
                .methodBuilder("saveItem")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(TypeName.BOOLEAN)
                .addStatement("return $T.saveItemTo(item, $L.class)", BaseModel.class, modelClass.simpleName())
                .build();
    }

    /**
     * For example, #buildDeleteItemMethod(ClassName.get(XXX.class)) will generates:
     *
     * > public static boolean deleteItem(XXX item) {
     * >     return BaseModel.deleteItemFrom(item, XXX.class);
     * > }
     */
    private MethodSpec buildDeleteItemMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(modelClass, "item").build();
        return MethodSpec
                .methodBuilder("deleteItem")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(TypeName.BOOLEAN)
                .addStatement("return $T.deleteItemFrom(item, $L.class)", BaseModel.class, modelClass.simpleName())
                .build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithFieldVariableNames = {{age, age}, {user_name, userName}}
     *
     * #buildToValueMapMethod() will generates:
     *
     * > @Override
     * > public ValueMap toValueMap() {
     * >     return super.toValueMap()
     * >             .put(FIELD_AGE, age)
     * >             .put(FIELD_USER_NAME, userName);
     * > }
     */
    private MethodSpec buildToValueMapMethod(
            Map<String, String> fieldNamesWithFieldNameConstantNames,
            Map<String, String> fieldNamesWithFieldVariableNames) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("toValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(ValueMap.class);

        builder.addCode("return super.toValueMap()\n");
        for (String fieldName : fieldNamesWithFieldNameConstantNames.keySet()) {
            builder.addCode(".put($L, $L)\n",
                    fieldNamesWithFieldNameConstantNames.get(fieldName),
                    fieldNamesWithFieldVariableNames.get(fieldName));
        }
        builder.addCode(";\n");

        return builder.build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithFieldVariableNames = {{age, age}, {user_name, userName}}
     * @param fieldNamesWithTypes = {{age, TypeName.INT}, {user_name, TypeName.get(String.class)}}
     *
     * #buildInitWithValueMapMethod() will generates:
     *
     * > @Override
     * > public void initWithValueMap(ValueMap valueMap) {
     * >     super.initWithValueMap();
     * >     age = valueMap.getAsInt(FIELD_AGE);
     * >     userName = valueMap.getAsString(FIELD_USER_NAME);
     * > }
     */
    private MethodSpec buildInitWithValueMapMethod(
            Map<String, String> fieldNamesWithFieldNameConstantNames,
            Map<String, String> fieldNamesWithFieldVariableNames,
            Map<String, TypeName> fieldNamesWithTypes) {

        ParameterSpec param = ParameterSpec.builder(ValueMap.class, "valueMap").build();

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("initWithValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(param);

        builder.addCode("super.initWithValueMap($N);\n", param);
        for (String fieldName : fieldNamesWithFieldNameConstantNames.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableNames.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantNames.get(fieldName);
            TypeName fieldType = fieldNamesWithTypes.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addCode("$L = valueMap.getAsInt($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addCode("$L = valueMap.getAsLong($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addCode("$L = valueMap.getAsBoolean($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addCode("$L = valueMap.getAsString($L);\n", fieldValName, fieldConstName);
            } else {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        fieldType.toString() + " type does not supported.");
            }
        }

        return builder.build();
    }

    /**
     * For example, #buildNewInstanceMethod(ClassName.get(XXX.class)) will generates:
     *
     * > static XXX newInstance() {
     * >     return new XXX();
     * > }
     */
    private MethodSpec buildNewInstanceMethod(ClassName modelClass) {
        return MethodSpec
                .methodBuilder("newInstance")
                .addModifiers(Modifier.STATIC)
                .returns(modelClass)
                .addStatement("return new $L()", modelClass.simpleName())
                .build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithTypes = {{age, TypeName.INT}, {user_name, TypeName.get(String.class)}}
     *
     * #buildGetFieldNamesAndTypesMethod() will generates:
     *
     * > static Map<\String, FieldType> getFieldNamesAndTypes() {
     * >     Map<\String, FieldType> map = new HashMap<>();
     * >     map.put(FIELD_AGE, FieldType.INT_TYPE);
     * >     map.put(FIELD_USER_NAME, FieldType.STRING_TYPE);
     * >     return map;
     * > }
     */
    private MethodSpec buildGetFieldNamesAndTypesMethod(
            Map<String, String> fieldNamesWithFieldNameConstantNames,
            Map<String, TypeName> fieldNamesWithTypes) {

        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                classMap, classString, classFieldType);

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("getFieldNamesAndTypes")
                .addModifiers(Modifier.STATIC)
                .returns(returnType);

        builder.addStatement("$T<$T, $T> map = new $T<>()",
                Map.class, String.class, FieldType.class, HashMap.class);

        for (String fieldName : fieldNamesWithFieldNameConstantNames.keySet()) {
            String fieldConstName = fieldNamesWithFieldNameConstantNames.get(fieldName);
            TypeName fieldType = fieldNamesWithTypes.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addStatement("map.put($L, $T.INT_TYPE)", fieldConstName, FieldType.class);
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addStatement("map.put($L, $T.LONG_TYPE)", fieldConstName, FieldType.class);
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addStatement("map.put($L, $T.BOOLEAN_TYPE)", fieldConstName, FieldType.class);
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addStatement("map.put($L, $T.STRING_TYPE)", fieldConstName, FieldType.class);
            } else {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        fieldType.toString() + " type does not supported.");
            }
        }

        builder.addStatement("return map");

        return builder.build();
    }
}
