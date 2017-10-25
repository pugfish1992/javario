# MEMO

まず、Schemaクラスを作成する.

```
// Specify a model name
@ModelSchema("Mario")
public class MarioSchema {

    // Define fields of the model
    String shout;
    boolean haveYoshi;
    long killCount;
    int lifeCount;
}
```

この場合、Javario は次のModelクラスを生成する:

```
public class Mario extends BaseModel {

  public static final String FIELD_LIFE_COUNT = "lifeCount";
  public static final String FIELD_WITH_LUIGI = "withLuigi";
  public static final String FIELD_KILL_COUNT = "killCount";
  public static final String FIELD_SHOUT = "shout";

  public int lifeCount;
  public boolean withLuigi;
  public int killCount;
  public String shout;

  public static Mario findItemById(long id) {
    return BaseModel.findByItemIdFrom(id, Mario.class);
  }

  public static List<Mario> listUpItems() {
    return BaseModel.listUpItemsFrom(Mario.class);
  }

  public static boolean saveItem(Mario item) {
    return BaseModel.saveItemTo(item, Mario.class);
  }

  public static boolean deleteItem(Mario item) {
    return BaseModel.deleteItemFrom(item, Mario.class);
  }

  @Override
  public final ValueMap toValueMap() {
    return super.toValueMap()
    .put(FIELD_LIFE_COUNT, lifeCount)
    .put(FIELD_WITH_LUIGI, withLuigi)
    .put(FIELD_KILL_COUNT, killCount)
    .put(FIELD_SHOUT, shout)
    ;
  }

  @Override
  public final void initWithValueMap(ValueMap valueMap) {
    super.initWithValueMap(valueMap);
    lifeCount = valueMap.getAsInt(FIELD_LIFE_COUNT);
    withLuigi = valueMap.getAsBoolean(FIELD_WITH_LUIGI);
    killCount = valueMap.getAsInt(FIELD_KILL_COUNT);
    shout = valueMap.getAsString(FIELD_SHOUT);
  }

  static Mario newInstance() {
    return new Mario();
  }

  static Map<String, FieldType> getFieldNamesAndTypes() {
    Map<String, FieldType> map = new HashMap<>();
    map.put(FIELD_ID, FieldType.INT_TYPE);
    map.put(FIELD_LIFE_COUNT, FieldType.INT_TYPE);
    map.put(FIELD_WITH_LUIGI, FieldType.BOOLEAN_TYPE);
    map.put(FIELD_KILL_COUNT, FieldType.INT_TYPE);
    map.put(FIELD_SHOUT, FieldType.STRING_TYPE);
    return map;
  }
}
```

メンバ変数の名前はSchemaクラスの物がそのまま受け継がれるが、修飾子は全て **public** となる.
デフォルトでは、フィールド名はメンバ変数名と同じになる.フィールド名とは、今回の場合で言うと`lifeCount`,`withLuigi`,`killCount`,`shout`.また
フィールド名を表すString定数の名前は、メンバ変数名を大文字のスネークケースに変換し、それに**FIELD**というprefixをつけたものになる.


```
  public static final String FIELD_LIFE_COUNT = "lifeCount";
  public static final String FIELD_WITH_LUIGI = "withLuigi";
  public static final String FIELD_KILL_COUNT = "killCount";
  public static final String FIELD_SHOUT = "shout";

  public int lifeCount;
  public boolean withLuigi;
  public int killCount;
  public String shout;
```

各フィールド名とString定数名は**@FieldOption**アノテーションを使って個別に指定することもできる.

```
@ModelSchema("Mario")
public class MarioSchema {

    @FieldOption(fieldName = "custom name of shout", constVarName = "CUSTOM_CONST_NAME_OF_SHOUT")
    String shout;

    @FieldOption(fieldName = "custom name of withLuigi")    
    boolean withLuigi;
    
    @FieldOption(constVarName = "CUSTOM_CONST_NAME_OF_KILL_COUNT")
    int killCount;
    
    int lifeCount;
}
```

上記のようにした場合、生成されるクラスの変数の定義部分は次のようになる. `fieldName`(または`constVarName`)に空文字を指定した場合は無視される.


```
  public static final String FIELD_LIFE_COUNT = "lifeCount";
  public static final String FIELD_WITH_LUIGI = "custom name of withLuigi";
  public static final String FIELD_CUSTOM_CONST_NAME_OF_KILL_COUNT = "killCount";
  public static final String FIELD_CUSTOM_CONST_NAME_OF_SHOUT = "custom name of shout";

  public int lifeCount;
  public boolean withLuigi;
  public int killCount;
  public String shout;
```

また、**@ModelSchema**アノテーションを使えば、prefixの指定もできる.

```
@ModelSchemaOption(constVarNamePrefix = "CUSTOM_PREFIX_")
@ModelSchema("Mario")
public class MarioSchema {

    @FieldOption(fieldName = "custom name of shout", constVarName = "CUSTOM_CONST_NAME_OF_SHOUT")
    String shout;

    @FieldOption(fieldName = "custom name of withLuigi")    
    boolean withLuigi;
    
    @FieldOption(constVarName = "CUSTOM_CONST_NAME_OF_KILL_COUNT")
    int killCount;
    
    int lifeCount;
}
```

この場合は次のようになる. `constVarNamePrefix`に空文字を指定することもできる.

```
  public static final String CUSTOM_PREFIX_LIFE_COUNT = "lifeCount";
  public static final String CUSTOM_PREFIX_WITH_LUIGI = "custom name of withLuigi";
  public static final String CUSTOM_PREFIX_CUSTOM_CONST_NAME_OF_KILL_COUNT = "killCount";
  public static final String CUSTOM_PREFIX_CUSTOM_CONST_NAME_OF_SHOUT = "custom name of shout";

  public int lifeCount;
  public boolean withLuigi;
  public int killCount;
  public String shout;
```