package com.pugfish1992.javario;

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

// In annotation processing, JavarioProcessor will generates a model class such as below:

public class Mario extends BaseModel {

    public static final String MARIO_ID = "marioId";
    public static final String LIFE = "life";
    public static final String IS_FIRE_MARIO = "isFireMario";

    public long marioId;
    public int life;
    public boolean isFireMario;

    // This empty public constructor is needed when
    // create a new instance of a model class using reflection.
    public Mario() {}

    // Return the model name which is specified.
    // in the schema class using @ModelSchema annotation
    public static String modelName() {
        return "Mario";
    }

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

    // Static delegate method.
    public static Mario findItem(long primaryKey) {
        return BaseModel.findItemFrom(primaryKey, Mario.class);
    }

    // Static delegate method.
    public static List<Mario> listItems() {
        return BaseModel.listItemsFrom(Mario.class);
    }

    // Static delegate method.
    public static boolean saveItem(Mario item) {
        return BaseModel.saveItemTo(item);
    }

    // Static delegate method.
    public static boolean deleteItem(Mario item) {
        return BaseModel.deleteItemFrom(item);
    }

    // Return the primary key which is specified
    // in the schema class using @PrimaryKey annotation.
    @Override
    public long getPrimaryKey() {
        return marioId;
    }

    // This method will be used for storing data of a model into the database.
    @Override
    public ValueMap toValueMap() {
        return new ValueMap()
                .put(MARIO_ID, marioId)
                .put(LIFE, life)
                .put(IS_FIRE_MARIO, isFireMario);
    }

    // This method will be used for restoring data of a model from the database.
    @Override
    public void initWithValueMap(ValueMap valueMap) {
        this.marioId = valueMap.getAsLong(MARIO_ID);
        this.life = valueMap.getAsInt(LIFE);
        this.isFireMario = valueMap.getAsBoolean(IS_FIRE_MARIO);
    }
}
 */

final class ModelClassSpec {

    /* Intentional private */
    private ModelClassSpec() {}

    static final String GENERATED_CLASS_PACKAGE = "com.pugfish1992.javario";

    static final String METHOD_RETURN_MODEL_NAME = "modelName";
    static final String METHOD_FIND_ITEM = "findItemById";
    static final String METHOD_LIST_ITEMS = "listItems";
    static final String METHOD_SAVE_ITEM = "saveItem";
    static final String METHOD_DELETE_ITEM = "deleteItem";
    static final String METHOD_CREATE_VALUE_MAP = "toValueMap";
    static final String METHOD_INIT_WITH_VALUE_MAP = "initWithValueMap";
    static final String METHOD_CREATE_NEW_INSTANCE = "newInstance";
    static final String METHOD_RETURN_SCHEMA_INFO = "getSchemaInfo";
}
