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
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.pugfish1992.javario.annotation.ModelSchema",
        "com.pugfish1992.javario.annotation.ModelSchemaOption",
        "com.pugfish1992.javario.annotation.FieldOption"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JavarioProcessor extends AbstractProcessor {

    private static final String GENERATED_CLASS_PACKAGE = BaseModel.class.getPackage().getName();
    private static final String DEF_PREFIX_OF_NAME_OF_CONST_VAR_FOR_FIELD_NAME = "FIELD_";

    private static final ClassName classString = ClassName.get(String.class);
    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classMap = ClassName.get(Map.class);
    private static final ClassName classFieldType = ClassName.get(FieldType.class);

    private Filer mFiler;
    private Messager mMessager;
//    private Elements mElements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
//        mElements = processingEnvironment.getElementUtils();
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

    /**
     *
     * @param typeElement One of this element's annotation would be {@link ModelSchema}.
     */
    private void writeModelClass(TypeElement typeElement) throws IOException {
        ClassName modelClassName = ClassName.get(
                GENERATED_CLASS_PACKAGE, AnnotationUtils.getModelName(typeElement));

        TypeSpec.Builder modelClass = TypeSpec
                .classBuilder(modelClassName.simpleName())
                .superclass(BaseModel.class)
                .addModifiers(Modifier.PUBLIC);

        // Key is a fieldName
        Map<String, String> fieldNamesWithFieldNameConstantVarNames = new HashMap<>();
        Map<String, String> fieldNamesWithFieldVariableNames = new HashMap<>();
        Map<String, TypeName> fieldNamesWithTypes = new HashMap<>();

        // prefix of a name of a constant variable which represent a field name
        String prefix = AnnotationUtils
                .getSpecifiedPrefixOfFieldNameConstVariableNamesIfExist(typeElement);
        if (prefix == null) {
            prefix = DEF_PREFIX_OF_NAME_OF_CONST_VAR_FOR_FIELD_NAME;
        }

        // Field name constants & fields
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                TypeName typeName = MetaDataUtils.getVariableType(variableElement);

                if (!MetaDataUtils.isTypeSupported(typeName)) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR,
                            typeName.toString() + " type does not supported.");
                }

                // name of a variable of a field
                String fieldVarName = MetaDataUtils.getFieldName(variableElement);

                // field name
                String fieldName = AnnotationUtils.getSpecifiedFieldNameIfExist(variableElement);
                if (fieldName == null) {
                    fieldName = fieldVarName;
                }

                // name of a constant variable which represent a field name
                String fieldNameConstVarName = AnnotationUtils
                        .getSpecifiedFieldNameConstVariableNameIfExist(variableElement);
                if (fieldNameConstVarName == null) {
                    fieldNameConstVarName = StringUtils.camelToCapitalSnake(fieldName);
                }
                fieldNameConstVarName = prefix + fieldNameConstVarName;

                fieldNamesWithTypes.put(fieldName, typeName);
                fieldNamesWithFieldVariableNames.put(fieldName, fieldVarName);
                fieldNamesWithFieldNameConstantVarNames.put(fieldName, fieldNameConstVarName);
            }
        }

        // Variables
        for (String fieldName : fieldNamesWithTypes.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableNames.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantVarNames.get(fieldName);
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
                fieldNamesWithFieldNameConstantVarNames, fieldNamesWithFieldVariableNames));
        modelClass.addMethod(buildInitWithValueMapMethod(
                fieldNamesWithFieldNameConstantVarNames, fieldNamesWithFieldVariableNames, fieldNamesWithTypes));
        modelClass.addMethod(buildNewInstanceMethod(modelClassName));
        modelClass.addMethod(buildGetFieldNamesAndTypesMethod(
                fieldNamesWithFieldNameConstantVarNames, fieldNamesWithTypes));

        JavaFile.builder(GENERATED_CLASS_PACKAGE, modelClass.build()).build().writeTo(mFiler);
    }

    /**
     * For example, #buildFieldNameConstant("user_name", "FIELD_USER_NAME) will generates:
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
     * > public static List<\XXX> listUpItems() {
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
     * @param fieldNamesWithFieldNameConstantVarNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
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
            Map<String, String> fieldNamesWithFieldNameConstantVarNames,
            Map<String, String> fieldNamesWithFieldVariableNames) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("toValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(ValueMap.class);

        builder.addCode("return super.toValueMap()\n");
        for (String fieldName : fieldNamesWithFieldNameConstantVarNames.keySet()) {
            builder.addCode(".put($L, $L)\n",
                    fieldNamesWithFieldNameConstantVarNames.get(fieldName),
                    fieldNamesWithFieldVariableNames.get(fieldName));
        }
        builder.addCode(";\n");

        return builder.build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantVarNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
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
            Map<String, String> fieldNamesWithFieldNameConstantVarNames,
            Map<String, String> fieldNamesWithFieldVariableNames,
            Map<String, TypeName> fieldNamesWithTypes) {

        ParameterSpec param = ParameterSpec.builder(ValueMap.class, "valueMap").build();

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("initWithValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(param);

        builder.addCode("super.initWithValueMap($N);\n", param);
        for (String fieldName : fieldNamesWithFieldNameConstantVarNames.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableNames.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantVarNames.get(fieldName);
            TypeName fieldType = fieldNamesWithTypes.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addCode("this.$L = valueMap.getAsInt($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addCode("this.$L = valueMap.getAsLong($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addCode("this.$L = valueMap.getAsBoolean($L);\n", fieldValName, fieldConstName);
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addCode("this.$L = valueMap.getAsString($L);\n", fieldValName, fieldConstName);
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
     * @param fieldNamesWithFieldNameConstantVarNames = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
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
            Map<String, String> fieldNamesWithFieldNameConstantVarNames,
            Map<String, TypeName> fieldNamesWithTypes) {

        ParameterizedTypeName returnType = ParameterizedTypeName
                .get(classMap, classString, classFieldType);

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("getFieldNamesAndTypes")
                .addModifiers(Modifier.STATIC)
                .returns(returnType);

        builder.addStatement("$T<$T, $T> map = new $T<>()",
                Map.class, String.class, FieldType.class, HashMap.class);
        builder.addStatement("map.put(FIELD_ID, $T.$L)", FieldType.class, FieldType.LONG_TYPE.name());

        for (String fieldName : fieldNamesWithFieldNameConstantVarNames.keySet()) {
            String fieldConstName = fieldNamesWithFieldNameConstantVarNames.get(fieldName);
            TypeName fieldType = fieldNamesWithTypes.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addStatement("map.put($L, $T.$L)", fieldConstName, FieldType.class, FieldType.INT_TYPE.name());
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addStatement("map.put($L, $T.$L)", fieldConstName, FieldType.class, FieldType.LONG_TYPE.name());
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addStatement("map.put($L, $T.$L)", fieldConstName, FieldType.class, FieldType.BOOLEAN_TYPE.name());
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addStatement("map.put($L, $T.$L)", fieldConstName, FieldType.class, FieldType.STRING_TYPE.name());
            } else {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        fieldType.toString() + " type does not supported.");
            }
        }

        builder.addStatement("return map");

        return builder.build();
    }
}
