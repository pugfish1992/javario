package com.pugfish1992.javario;

import android.content.ContentValues;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;
import com.pugfish1992.javario.annotation.PrimaryKey;
import com.pugfish1992.javario.datasource.FieldType;
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

/*

// Let's say there is a schema class such as below:

@ModelSchema(value = "Mario")
public class MarioSchema {

    // Schema class must has ONE primary key (declared as long).
    @PrimaryKey
    long marioId;

    int life;
    boolean isFireMario;
}

// In annotation processing, This class will generates a model class such as below:

public class Mario extends BaseModel {

    // 1- [constant strings]
    public static final String MARIO_ID = "marioId";
    public static final String LIFE = "life";
    public static final String IS_FIRE_MARIO = "isFireMario";

    // 2- [variables]
    public long marioId;
    public int life;
    public boolean isFireMario;

    // 3- [default empty constructor]
    // This empty public constructor is needed when
    // create a new instance of a model class using reflection.
    public Mario() {}

    // 4- [getting model name method]
    // Return the model name which is specified.
    // in the schema class using @ModelSchema annotation
    public static String modelName() {
        return "Mario";
    }

    // 5- [getting schema info method]
    // Return the schema info.
    public static SchemaInfo getSchemaInfo() {
        SchemaInfo info = new SchemaInfo();
        info.setModelName(Mario.modelName());
        info.setClassName(Mario.class.getSimpleName());
        info.addFieldNameAndType(MARIO_ID, FieldType.LONG);
        info.addFieldNameAndType(LIFE, FieldType.INT);
        info.addFieldNameAndType(IS_FIRE_MARIO, FieldType.BOOLEAN);
        return info;
    }

    // 6- [getting an item method]
    // Static delegate method.
    public static Mario findItem(long primaryKey) {
        return BaseModel.findItemFrom(primaryKey, Mario.class);
    }

    // 7- [getting list of items method]
    // Static delegate method.
    public static List<Mario> listItems() {
        return BaseModel.listItemsFrom(Mario.class);
    }

    // 8- [saving an item method]
    // Static delegate method.
    public static boolean saveItem(Mario item) {
        return BaseModel.saveItem(item);
    }

    // 9- [deleting an item method]
    // Static delegate method.
    public static boolean deleteItem(Mario item) {
        return BaseModel.deleteItem(item);
    }

    // 10- [getting the primary key method]
    // Return the primary key which is specified
    // in the schema class using @PrimaryKey annotation.
    @Override
    public final long getPrimaryKey() {
        return marioId;
    }

    // 11- [storing data method]
    // This method will be used for storing data of a model into the database.
    @Override
    public final ContentValues toValueMap() {
        return new ContentValues()
                .put(MARIO_ID, marioId)
                .put(LIFE, life)
                .put(IS_FIRE_MARIO, isFireMario);
    }

    // 12- [restoring data method]
    // This method will be used for restoring data of a model from the database.
    @Override
    public final void initWithValueMap(ContentValues valueMap) {
        this.marioId = valueMap.getAsLong(MARIO_ID);
        this.life = valueMap.getAsInt(LIFE);
        this.isFireMario = valueMap.getAsBoolean(IS_FIRE_MARIO);
    }

    // 13- [setting the primary key method]
    @Override
    public final void setPrimaryKey(long primaryKey) {
        marioId = primaryKey;
    }
}
 */

class ModelClassWriter {

    private static final ClassName classString = ClassName.get(String.class);
    private static final ClassName classList = ClassName.get(List.class);
    private static final ClassName classSchemaInfo = ClassName.get(SchemaInfo.class);
    private static final ClassName classContentValues = ClassName.get(ContentValues.class);
    private static final ClassName classBaseModel = ClassName.get(BaseModel.class);
    private static final ClassName classFieldType = ClassName.get(FieldType.class);

    private static final String CONST_STRING_EMPTY_PREFIX = "";

    private Messager mMessager;
    private Filer mFiler;

    ModelClassWriter(Messager messager, Filer filer) {
        mMessager = messager;
        mFiler = filer;
    }

    /**
     * @param typeElement One of this element's annotation would be {@link ModelSchema}.
     * @return Return the name of a written class, or null if any error occurred.
     */
    String write(TypeElement typeElement, String packageName) throws IOException {
        ModelSchema modelSchemaAnno = typeElement.getAnnotation(ModelSchema.class);
        if (modelSchemaAnno == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    typeElement.getSimpleName() + " does not have "
                            + ModelSchema.class.getSimpleName() + " annotation.");

            return null;
        }
        
        String modelName = modelSchemaAnno.value();
        if (modelName.length() == 0) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Specify a model name");
            return null;
        }

        // The name of a model class is the same as the model name if it is not specified
        String className = modelSchemaAnno.className();
        if (className.length() == 0) {
            // Make the first letter of a class name uppercase
            className = modelName.substring(0, 1).toUpperCase() + modelName.substring(1);
        }

        ClassName classNameObj = ClassName.get(packageName, className);

        TypeSpec.Builder modelClass = TypeSpec
                .classBuilder(className)
                .superclass(classBaseModel)
                .addModifiers(Modifier.PUBLIC);

        // Key is fieldNames
        Map<String, String> fieldNamesWithConstStringName = new HashMap<>();
        Map<String, String> fieldNamesWithVarName = new HashMap<>();
        Map<String, TypeName> fieldNamesWithVarType = new HashMap<>();
        Map<String, Object> fieldNamesWithDefaultValue = new HashMap<>();

        // prefix of a name of constant strings (if specified)
        ModelSchemaOption modelSchemaOptionAnno = typeElement.getAnnotation(ModelSchemaOption.class);
        String constStringPrefix = (modelSchemaOptionAnno != null)
                ? modelSchemaOptionAnno.constStringNamePrefix()
                : CONST_STRING_EMPTY_PREFIX;

        // a field name which has a @PrimaryKey annotation
        String nameOfPrimaryKeyField = null;

        // Constant strings & variables
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                TypeName varType = MetaDataUtils.getVariableType(variableElement);

                if (!MetaDataUtils.isSupportedType(varType)) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR,
                            varType.toString() + " type does not supported.");
                    return null;
                }

                // name of a variable of a field
                String fieldVarName = MetaDataUtils.getVariableName(variableElement);

                FieldOption fieldOptionAnno = variableElement.getAnnotation(FieldOption.class);

                // field name
                String fieldName = (fieldOptionAnno != null && fieldOptionAnno.fieldName().length() != 0)
                        ? fieldOptionAnno.fieldName()
                        : fieldVarName;

                // name of a constant variable which represent a field name
                String constStringName = (fieldOptionAnno != null && fieldOptionAnno.constStringName().length() != 0)
                        ? constStringPrefix + fieldOptionAnno.constStringName()
                        : constStringPrefix + StringUtils.camelToCapitalSnake(fieldName);

                fieldNamesWithVarType.put(fieldName, varType);
                fieldNamesWithVarName.put(fieldName, fieldVarName);
                fieldNamesWithConstStringName.put(fieldName, constStringName);

                // get default value if exists
                if (variableElement.getConstantValue() != null) {
                    fieldNamesWithDefaultValue.put(fieldName, variableElement.getConstantValue());
                }

                // Is the primary key or not
                PrimaryKey primaryKeyAnno = variableElement.getAnnotation(PrimaryKey.class);
                if (primaryKeyAnno != null) {
                    if (nameOfPrimaryKeyField != null) {
                        mMessager.printMessage(Diagnostic.Kind.ERROR,
                                "One schema class can not annotate two or more fields " +
                                        "with @" + PrimaryKey.class.getSimpleName() + " annotation");
                        return null;
                    }
                    if (!varType.equals(TypeName.LONG)) {
                        mMessager.printMessage(Diagnostic.Kind.ERROR,
                                "A field specified as the primary key must be declared as 'long'");
                        return null;
                    }

                    nameOfPrimaryKeyField = fieldName;
                }
            }
        }

        if (nameOfPrimaryKeyField == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    typeElement.getSimpleName() + " class must has one field " +
                            "which has a @" + PrimaryKey.class.getSimpleName() + " annotation");
            return null;
        }

        // Define Variables
        for (String fieldName : fieldNamesWithVarName.keySet()) {
            String fieldVarName = fieldNamesWithVarName.get(fieldName);
            String constStringName = fieldNamesWithConstStringName.get(fieldName);
            TypeName varType = fieldNamesWithVarType.get(fieldName);
            Object defValue = fieldNamesWithDefaultValue.get(fieldName);

            // [1] -> See comments at the top of this file
            modelClass.addField(buildConstString(fieldName, constStringName));
            // [2]
            modelClass.addField(buildVariable(fieldVarName, varType, defValue));
        }

        // [3]
        modelClass.addMethod(buildDefaultEmptyConstructor());
        // [4]
        modelClass.addMethod(buildGettingModelNameMethod(modelName));
        // [5]
        modelClass.addMethod(buildGettingSchemaInfoMethod(classNameObj,
                fieldNamesWithConstStringName.get(nameOfPrimaryKeyField),
                fieldNamesWithConstStringName, fieldNamesWithVarType));
        // [6]
        modelClass.addMethod(buildGettingItemMethod(classNameObj));
        // [7]
        modelClass.addMethod(buildGettingListOfItemsMethod(classNameObj));
        // [8]
        modelClass.addMethod(buildSavingItemMethod(classNameObj));
        // [9]
        modelClass.addMethod(buildDeletingItemMethod(classNameObj));
        // [10]
        modelClass.addMethod(buildGettingPrimaryKeyMethod(fieldNamesWithVarName.get(nameOfPrimaryKeyField)));
        // [11]
        modelClass.addMethod(buildStoringDataMethod(fieldNamesWithConstStringName, fieldNamesWithVarName));
        // [12]
        modelClass.addMethod(buildRestoringDataMethod(fieldNamesWithConstStringName, fieldNamesWithVarName, fieldNamesWithVarType));
        // [13]
        modelClass.addMethod(buildSettingPrimaryKeyMethod(fieldNamesWithVarName.get(nameOfPrimaryKeyField)));

        JavaFile.builder(packageName, modelClass.build()).build().writeTo(mFiler);
        return className;
    }

    private FieldSpec buildConstString(String fieldName, String constStringName) {
        return FieldSpec.builder(classString,
                constStringName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", fieldName)
                .build();
    }

    private FieldSpec buildVariable(String variableName, TypeName varType, Object defValue) {
        FieldSpec.Builder builder = FieldSpec.builder(varType, variableName, Modifier.PUBLIC);
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

    private MethodSpec buildDefaultEmptyConstructor() {
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private MethodSpec buildGettingModelNameMethod(String modelName) {
        return MethodSpec
                .methodBuilder("modelName")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classString)
                .addStatement("return $S", modelName)
                .build();
    }

    private MethodSpec buildGettingSchemaInfoMethod(
            ClassName modelClass,
            String constStringNameOfPrimaryKeyField,
            Map<String, String> fieldNamesWithConstStringName,
            Map<String, TypeName> fieldNamesWithType) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("getSchemaInfo")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(classSchemaInfo);

        builder.addStatement("$T info = new $T()", classSchemaInfo, classSchemaInfo);
        // This statement depends on #modelName() method which will be generated by the processor
        builder.addStatement("info.setModelName($L.modelName())", modelClass.simpleName());
        builder.addStatement("info.setClassName($L.class.getSimpleName())", modelClass.simpleName());
        builder.addStatement("info.setNameOfPrimaryKeyField($L)", constStringNameOfPrimaryKeyField);
        for (String fieldName : fieldNamesWithConstStringName.keySet()) {
            String fieldConstName = fieldNamesWithConstStringName.get(fieldName);
            TypeName fieldType = fieldNamesWithType.get(fieldName);

            if (MetaDataUtils.isIntType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, classFieldType, FieldType.INT.name());
            } else
            if (MetaDataUtils.isLongType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, classFieldType, FieldType.LONG.name());
            } else
            if (MetaDataUtils.isBooleanType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, classFieldType, FieldType.BOOLEAN.name());
            } else
            if (MetaDataUtils.isStringType(fieldType)) {
                builder.addStatement("info.addFieldNameAndType($L, $T.$L)",
                        fieldConstName, classFieldType, FieldType.STRING.name());
            }
        }
        builder.addStatement("return info");

        return builder.build();
    }

    private MethodSpec buildGettingItemMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(TypeName.LONG, "primaryKey").build();
        return MethodSpec
                .methodBuilder("findItem")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(modelClass)
                .addStatement("return $T.findItemFrom(primaryKey, $L.class)", classBaseModel, modelClass.simpleName())
                .build();
    }

    private MethodSpec buildGettingListOfItemsMethod(ClassName modelClass) {
        return MethodSpec
                .methodBuilder("listItems")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(classList, modelClass))
                .addStatement("return $T.listItemsFrom($L.class)", classBaseModel, modelClass.simpleName())
                .build();
    }

    private MethodSpec buildSavingItemMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(modelClass, "item").build();
        return MethodSpec
                .methodBuilder("saveItem")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(TypeName.BOOLEAN)
                .addStatement("return $T.saveItem(item)", classBaseModel)
                .build();
    }

    private MethodSpec buildDeletingItemMethod(ClassName modelClass) {
        ParameterSpec param = ParameterSpec.builder(modelClass, "item").build();
        return MethodSpec
                .methodBuilder("deleteItem")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(TypeName.BOOLEAN)
                .addStatement("return $T.deleteItem(item)", classBaseModel)
                .build();
    }

    private MethodSpec buildGettingPrimaryKeyMethod(String primaryKeyVarName) {
        return MethodSpec
                .methodBuilder("getPrimaryKey")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(TypeName.LONG)
                .addStatement("return $L", primaryKeyVarName)
                .build();
    }

    private MethodSpec buildStoringDataMethod(
            Map<String, String> fieldNamesWithConstStringName,
            Map<String, String> fieldNamesWithVarName) {

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("toValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(classContentValues);

        builder.addStatement("$T map = new $T()", classContentValues, classContentValues);
        for (String fieldName : fieldNamesWithConstStringName.keySet()) {
            builder.addStatement("map.put($L, $L)",
                    fieldNamesWithConstStringName.get(fieldName),
                    fieldNamesWithVarName.get(fieldName));
        }
        builder.addStatement("return map");

        return builder.build();
    }

    private MethodSpec buildRestoringDataMethod(
            Map<String, String> fieldNamesWithConstStringName,
            Map<String, String> fieldNamesWithVarName,
            Map<String, TypeName> fieldNamesWithType) {

        ParameterSpec param = ParameterSpec.builder(classContentValues, "valueMap").build();

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder("initWithValueMap")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(param);

        for (String fieldName : fieldNamesWithConstStringName.keySet()) {
            String varName = fieldNamesWithVarName.get(fieldName);
            String constStringName = fieldNamesWithConstStringName.get(fieldName);
            TypeName varType = fieldNamesWithType.get(fieldName);

            if (MetaDataUtils.isIntType(varType)) {
                builder.addStatement("this.$L = valueMap.getAsInteger($L)", varName, constStringName);
            } else
            if (MetaDataUtils.isLongType(varType)) {
                builder.addStatement("this.$L = valueMap.getAsLong($L)", varName, constStringName);
            } else
            if (MetaDataUtils.isBooleanType(varType)) {
                builder.addStatement("this.$L = valueMap.getAsBoolean($L)", varName, constStringName);
            } else
            if (MetaDataUtils.isStringType(varType)) {
                builder.addStatement("this.$L = valueMap.getAsString($L)", varName, constStringName);
            } else {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        varType.toString() + " type does not supported.");
            }
        }

        return builder.build();
    }

    private MethodSpec buildSettingPrimaryKeyMethod(String primaryKeyVarName) {
        return MethodSpec
                .methodBuilder("setPrimaryKey")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(TypeName.LONG, "primaryKey").build())
                .addStatement("$L = primaryKey", primaryKeyVarName)
                .build();
    }
}
