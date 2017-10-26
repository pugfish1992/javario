package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.datasource.FieldType;
import com.pugfish1992.javario.datasource.ValueMap;
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

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * Created by daichi on 10/27/17.
 */

class ModelClassWriter {

    private static final String GENERATED_CLASS_PACKAGE = BaseModel.class.getPackage().getName();
    private static final String DEF_PREFIX_OF_NAME_OF_CONST_VAR_FOR_FIELD_NAME = "FIELD_";

    private static final ClassName classString = ClassName.get(String.class);
    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classMap = ClassName.get(Map.class);
    private static final ClassName classFieldType = ClassName.get(FieldType.class);
    private static final ClassName classSchemaInfo = ClassName.get(SchemaInfo.class);

    private Messager mMessager;
    private Filer mFiler;

    ModelClassWriter(Messager messager, Filer filer) {
        mMessager = messager;
        mFiler = filer;
    }

    /**
     * @param typeElement One of this element's annotation would be {@link ModelSchema}.
     * @return A pair of a model name and model class name.
     */
    Map.Entry<String, String> write(TypeElement typeElement) throws IOException {
        final Map.Entry<String, String> modelNameAndClassName = AnnotationUtils.getModelNameAndClassName(typeElement);
        if (modelNameAndClassName.getKey() == null) {
            throw new IllegalStateException("Specify a model name");
        }
        // The name of a model class is the same as the model name if it is not specified
        if (modelNameAndClassName.getValue() == null) {
            modelNameAndClassName.setValue(modelNameAndClassName.getKey());
        }

        ClassName modelClassName = ClassName.get(GENERATED_CLASS_PACKAGE, modelNameAndClassName.getValue());

        TypeSpec.Builder modelClass = TypeSpec
                .classBuilder(modelClassName.simpleName())
                .superclass(BaseModel.class)
                .addModifiers(Modifier.PUBLIC);

        // Key is a fieldName
        Map<String, String> fieldNamesWithFieldNameConstantVarName = new HashMap<>();
        Map<String, String> fieldNamesWithFieldVariableName = new HashMap<>();
        Map<String, TypeName> fieldNamesWithType = new HashMap<>();
        Map<String, Object> fieldNamesWithDefaultValue = new HashMap<>();

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

                if (!MetaDataUtils.isSupportedType(typeName)) {
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

                fieldNamesWithType.put(fieldName, typeName);
                fieldNamesWithFieldVariableName.put(fieldName, fieldVarName);
                fieldNamesWithFieldNameConstantVarName.put(fieldName, fieldNameConstVarName);

                // get default value if exists
                if (variableElement.getConstantValue() != null) {
                    fieldNamesWithDefaultValue.put(fieldName, variableElement.getConstantValue());
                }
            }
        }

        // Variables
        for (String fieldName : fieldNamesWithType.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableName.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantVarName.get(fieldName);
            TypeName fieldType = fieldNamesWithType.get(fieldName);
            Object defValue = fieldNamesWithDefaultValue.get(fieldName);

            modelClass.addField(buildFieldNameConstantVariable(fieldName, fieldConstName));
            modelClass.addField(buildFieldVariable(fieldValName, fieldType, defValue));
        }

        // Method to get the model name
        modelClass.addMethod(buildModelNameMethod(modelNameAndClassName.getKey()));

        // Methods for CRUD
        modelClass.addMethod(buildFindItemByIdMethod(modelClassName));
        modelClass.addMethod(buildListItemsMethod(modelClassName));
        modelClass.addMethod(buildSaveItemMethod(modelClassName));
        modelClass.addMethod(buildDeleteItemMethod(modelClassName));

        // Utility methods for DataSource
        modelClass.addMethod(buildToValueMapMethod(
                fieldNamesWithFieldNameConstantVarName, fieldNamesWithFieldVariableName));
        modelClass.addMethod(buildInitWithValueMapMethod(
                fieldNamesWithFieldNameConstantVarName, fieldNamesWithFieldVariableName, fieldNamesWithType));
        modelClass.addMethod(buildNewInstanceMethod(modelClassName));
        modelClass.addMethod(buildGetSchemaInfoMethod(
                modelClassName, fieldNamesWithFieldNameConstantVarName, fieldNamesWithType));

        JavaFile.builder(GENERATED_CLASS_PACKAGE, modelClass.build()).build().writeTo(mFiler);
        return modelNameAndClassName;
    }

    /**
     * For example, #buildFieldNameConstantVariable("user_name", "FIELD_USER_NAME") will generates:
     *
     * > public static final String FIELD_USER_NAME = "userName";
     *
     */
    private FieldSpec buildFieldNameConstantVariable(String fieldName, String constVariableName) {
        return FieldSpec.builder(String.class,
                constVariableName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", fieldName)
                .build();
    }

    /**
     * For example, #buildFieldVariable("age", TypeName.INT, 47) will generates:
     *
     * > public int age = 47;
     *
     */
    private FieldSpec buildFieldVariable(String variableName, TypeName type, Object defValue) {
        FieldSpec.Builder builder = FieldSpec.builder(type, variableName, Modifier.PUBLIC);
        if (defValue == null) {
            return builder.build();
        }

        if (defValue instanceof String) {
            return builder
                    .initializer("$S", (String) defValue)
                    .build();
        } else {
            return builder
                    .initializer("$L", defValue)
                    .build();
        }
    }

    /**
     * For example, #buildModelNameMethod("Mario") will generates:
     *
     * > public static String modelName() {
     * > return "Mario";
     * > }
     *
     */
    private MethodSpec buildModelNameMethod(String className) {
        return MethodSpec
                .methodBuilder("modelName")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classString)
                .addStatement("return $S", className)
                .build();
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
     * For example, #buildListItemsMethod(ClassName.get(XXX.class)) will generates:
     *
     * > public static List<\XXX> listItems() {
     * >     return BaseModel.listItemsFrom(id, XXX.class);
     * > }
     */
    private MethodSpec buildListItemsMethod(ClassName modelClass) {
        return MethodSpec
                .methodBuilder("listItems")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(classList, modelClass))
                .addStatement("return $T.listItemsFrom($L.class)", BaseModel.class, modelClass.simpleName())
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
                .addStatement("return $T.saveItemTo(item)", BaseModel.class)
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
                .addStatement("return $T.deleteItemFrom(item)", BaseModel.class)
                .build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantVarName = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithFieldVariableName = {{age, age}, {user_name, userName}}
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
            Map<String, String> fieldNamesWithFieldNameConstantVarName,
            Map<String, String> fieldNamesWithFieldVariableName) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("toValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(ValueMap.class);

        builder.addCode("return super.toValueMap()\n");
        for (String fieldName : fieldNamesWithFieldNameConstantVarName.keySet()) {
            builder.addCode(".put($L, $L)\n",
                    fieldNamesWithFieldNameConstantVarName.get(fieldName),
                    fieldNamesWithFieldVariableName.get(fieldName));
        }
        builder.addCode(";\n");

        return builder.build();
    }

    /**
     * For example,
     *
     * @param fieldNamesWithFieldNameConstantVarName = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithFieldVariableName = {{age, age}, {user_name, userName}}
     * @param fieldNamesWithType = {{age, TypeName.INT}, {user_name, TypeName.get(String.class)}}
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
            Map<String, String> fieldNamesWithFieldNameConstantVarName,
            Map<String, String> fieldNamesWithFieldVariableName,
            Map<String, TypeName> fieldNamesWithType) {

        ParameterSpec param = ParameterSpec.builder(ValueMap.class, "valueMap").build();

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("initWithValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(param);

        builder.addCode("super.initWithValueMap($N);\n", param);
        for (String fieldName : fieldNamesWithFieldNameConstantVarName.keySet()) {
            String fieldValName = fieldNamesWithFieldVariableName.get(fieldName);
            String fieldConstName = fieldNamesWithFieldNameConstantVarName.get(fieldName);
            TypeName fieldType = fieldNamesWithType.get(fieldName);

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
     * @param modelClass = ClassName.get(XXX.class)
     * @param fieldNamesWithFieldNameConstantVarName = {{age, FIELD_AGE}, {user_name, FIELD_USER_NAME}}
     * @param fieldNamesWithType = {{age, TypeName.INT}, {user_name, TypeName.get(String.class)}}
     *
     * #buildGetFieldNamesWithTypeMethod() will generates:
     *
     * > public static SchemaInfo getSchemaInfo() {
     * >     SchemaInfo info = new SchemaInfo();
     * >     info.setClassName(XXX.modelName());
     * >     info.setModelName(XXX.class.getSimpleName());
     * >     info.addFieldNameAndType(FIELD_AGE, FieldType.INT_TYPE);
     * >     info.addFieldNameAndType(FIELD_USER_NAME, FieldType.STRING_TYPE);
     * >     return info;
     * > }
     */
    private MethodSpec buildGetSchemaInfoMethod(
            ClassName modelClass,
            Map<String, String> fieldNamesWithFieldNameConstantVarName,
            Map<String, TypeName> fieldNamesWithType) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("getSchemaInfo")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classSchemaInfo);

        builder.addStatement("$T info = new $T()", classSchemaInfo, classSchemaInfo);
        // This statement depends on #modelName() method which will be generated by the processor
        builder.addStatement("info.setModelName($L.modelName())", modelClass.simpleName());
        builder.addStatement("info.setClassName($L.class.getSimpleName())", modelClass.simpleName());
        builder.addStatement("info.addFieldNameAndType(FIELD_ID, $T.$L)", FieldType.class, FieldType.LONG_TYPE.name());
        for (String fieldName : fieldNamesWithFieldNameConstantVarName.keySet()) {
            String fieldConstName = fieldNamesWithFieldNameConstantVarName.get(fieldName);
            TypeName fieldType = fieldNamesWithType.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, FieldType.class, FieldType.INT_TYPE.name());
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, FieldType.class, FieldType.LONG_TYPE.name());
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, FieldType.class, FieldType.BOOLEAN_TYPE.name());
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, FieldType.class, FieldType.STRING_TYPE.name());
            } else {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        fieldType.toString() + " type does not supported.");
            }
        }
        builder.addStatement("return info");

        return builder.build();
    }
}