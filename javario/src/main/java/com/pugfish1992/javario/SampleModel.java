package com.pugfish1992.javario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PlumberProcessor class generates a class like:
 */

public class SampleModel extends BaseModel {

    public static final String FIELD_COUNT_OF_SUBSCRIBERS = "countOfSubscribers";
    public static final String FIELD_USER_NAME = "userName";

    public int countOfSubscribers;
    public String userName;

    public static SampleModel findItemById(long id) {
        return BaseModel.findByItemIdFrom(id, SampleModel.class);
    }

    public static List<SampleModel> listUpItems() {
        return BaseModel.listUpItemsFrom(SampleModel.class);
    }

    public static boolean saveItem(SampleModel item) {
        return BaseModel.saveItemTo(item, SampleModel.class);
    }

    public static boolean deleteItem(SampleModel item) {
        return BaseModel.deleteItemFrom(item, SampleModel.class);
    }

    @Override
    public ValueMap toValueMap() {
        return super.toValueMap()
                .put(FIELD_COUNT_OF_SUBSCRIBERS, countOfSubscribers)
                .put(FIELD_USER_NAME, userName);
    }

    @Override
    public void initWithValueMap(ValueMap valueMap) {
        super.initWithValueMap(valueMap);
        countOfSubscribers = valueMap.getAsInt(FIELD_COUNT_OF_SUBSCRIBERS);
        userName = valueMap.getAsString(FIELD_USER_NAME);
    }

    /**
     * This method will be called in {@link ModelUtils#newInstanceOf(Class)}
     * using reflection.
     */
    static SampleModel newInstance() {
        return new SampleModel();
    }

    /**
     * This method will be called in {@link ModelUtils#getFiledNamesAndTypesOf(Class)}
     * using reflection.
     */
    static Map<String, FieldType> getFieldNamesAndTypes() {
        Map<String, FieldType> map = new HashMap<>();
        map.put(FIELD_ID, FieldType.LONG_TYPE);
        map.put(FIELD_COUNT_OF_SUBSCRIBERS, FieldType.INT_TYPE);
        map.put(FIELD_USER_NAME, FieldType.STRING_TYPE);
        return map;
    }
}
