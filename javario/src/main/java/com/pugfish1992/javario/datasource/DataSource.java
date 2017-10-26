package com.pugfish1992.javario.datasource;

import com.pugfish1992.javario.BaseModel;
import com.pugfish1992.javario.SchemaInfo;

import java.util.List;

/**
 * Created by daichi on 10/25/17.
 */

public interface DataSource<T extends BaseModel> {

    long INVALID_ID = -1;

    void onInitialize(List<SchemaInfo> schemaInfoList);
    T findItemByIdFrom(long id, Class<? extends BaseModel> klass);
    List<T> listItemsFrom(Class<? extends BaseModel> klass);
    boolean saveItemTo(T item);
    boolean deleteItemFrom(T item);
}
