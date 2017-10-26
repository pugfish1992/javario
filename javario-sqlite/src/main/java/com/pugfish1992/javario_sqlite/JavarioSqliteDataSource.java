package com.pugfish1992.javario_sqlite;

import com.pugfish1992.javario.BaseModel;
import com.pugfish1992.javario.SchemaInfo;
import com.pugfish1992.javario.datasource.DataSource;

import java.util.List;

/**
 * Created by daichi on 10/26/17.
 */

public class JavarioSqliteDataSource implements DataSource<BaseModel> {

    @Override
    public void onInitialize(List<SchemaInfo> schemaInfoList) {
    }

    @Override
    public BaseModel findItemByIdFrom(long id, Class<? extends BaseModel> klass) {
        return null;
    }

    @Override
    public List<BaseModel> listItemsFrom(Class<? extends BaseModel> klass) {
        return null;
    }

    @Override
    public boolean saveItemTo(BaseModel item) {
        return false;
    }

    @Override
    public boolean deleteItemFrom(BaseModel item) {
        return false;
    }
}
