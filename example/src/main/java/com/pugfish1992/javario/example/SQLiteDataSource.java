package com.pugfish1992.javario.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pugfish1992.javario.BaseModel;
import com.pugfish1992.javario.SchemaInfo;
import com.pugfish1992.javario.datasource.DataSource;
import com.pugfish1992.javario.datasource.ModelUtils;
import com.pugfish1992.sqliteutils.library.Query;
import com.pugfish1992.sqliteutils.library.WhereClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daichi on 10/28/17.
 */

public class SQLiteDataSource implements DataSource<BaseModel> {

    private DbOpenHelper mDbOpenHelper;
    private final Context mAppContext;
    private Map<String, SchemaInfo> mModelNamesWithSchemaInfo;

    public SQLiteDataSource(Context appContext) {
        mAppContext = appContext;
    }

    @Override
    public void onInitialize(List<SchemaInfo> schemaInfoList) {
        mDbOpenHelper = new DbOpenHelper(mAppContext, schemaInfoList);

        mModelNamesWithSchemaInfo = new HashMap<>();
        for (SchemaInfo info : schemaInfoList) {
            mModelNamesWithSchemaInfo.put(info.getClassName(), info);
        }
    }

    @Override
    public BaseModel findItemFrom(long id, final Class<? extends BaseModel> klass) {
        SchemaInfo info = mModelNamesWithSchemaInfo.get(klass.getSimpleName());

        WhereClause where = new WhereClause()
                .equalTo(info.getNameOfPrimaryKeyField(), id);

        Query query = new Query()
                .addTables(info.getModelName())
                .setSelection(where.toStatement());

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        final List<BaseModel> models = new ArrayList<>();
        query.startQueryAndScanResult(db, new Query.ScanningResultCallback() {
            @Override
            public void onStartScanning(Cursor cursor) {
                if (1 < cursor.getCount()) {
                    // Should not be reached
                    throw new IllegalStateException("Specified id is not unique");
                }
            }

            @Override
            public void onScanRow(ContentValues valueMap) {
                models.add(createModelFromValueMap(valueMap, klass));
            }

            @Override
            public boolean onEndScanning(Cursor cursor) {
                cursor.close();
                return false;
            }
        });

        db.close();
        return (models.size() != 0) ? models.get(0) : null;
    }

    @Override
    public List<BaseModel> listItemsFrom(final Class<? extends BaseModel> klass) {
        SchemaInfo info = mModelNamesWithSchemaInfo.get(klass.getSimpleName());

        final List<BaseModel> models = new ArrayList<>();
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        new Query()
                .addTables(info.getModelName())
                .startQueryAndScanResult(db, new Query.ScanningResultCallback() {
                    @Override
                    public void onStartScanning(Cursor cursor) {}

                    @Override
                    public void onScanRow(ContentValues valueMap) {
                        models.add(createModelFromValueMap(valueMap, klass));
                    }

                    @Override
                    public boolean onEndScanning(Cursor cursor) {
                        cursor.close();
                        return false;
                    }
                });

        db.close();
        return (models.size() != 0) ? models : null;
    }

    @Override
    public boolean saveItemTo(BaseModel item) {
        SchemaInfo info = mModelNamesWithSchemaInfo.get(item.getClass().getSimpleName());
        WhereClause where = new WhereClause()
                .equalTo(info.getNameOfPrimaryKeyField(), item.getPrimaryKey());

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        db.close();
        return false;
    }

    @Override
    public boolean deleteItemFrom(BaseModel item) {
        return false;
    }

    private BaseModel createModelFromValueMap(
            ContentValues valueMap, Class<? extends BaseModel> klass) {

        BaseModel model = ModelUtils.newInstanceOf(klass);
        model.initWithValueMap(valueMap);
        return model;
    }
}
