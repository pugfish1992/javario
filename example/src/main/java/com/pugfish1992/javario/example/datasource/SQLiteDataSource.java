package com.pugfish1992.javario.example.datasource;

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
    private Map<String, SchemaInfo> mClassNamesWithSchemaInfo;

    public SQLiteDataSource(Context appContext) {
        mAppContext = appContext;
    }

    @Override
    public void onInitialize(List<SchemaInfo> schemaInfoList) {
        mDbOpenHelper = new DbOpenHelper(mAppContext, schemaInfoList);

        mClassNamesWithSchemaInfo = new HashMap<>();
        for (SchemaInfo info : schemaInfoList) {
            mClassNamesWithSchemaInfo.put(info.getClassName(), info);
        }
    }

    @Override
    public BaseModel findItemFrom(long id, final Class<? extends BaseModel> klass) {
        SchemaInfo info = mClassNamesWithSchemaInfo.get(klass.getSimpleName());

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
                BaseModel model = ModelUtils.newInstanceOf(klass);
                model.initWithValueMap(valueMap);
                models.add(model);
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
        SchemaInfo info = mClassNamesWithSchemaInfo.get(klass.getSimpleName());

        final List<BaseModel> models = new ArrayList<>();
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        new Query()
                .addTables(info.getModelName())
                .startQueryAndScanResult(db, new Query.ScanningResultCallback() {
                    @Override
                    public void onStartScanning(Cursor cursor) {}

                    @Override
                    public void onScanRow(ContentValues valueMap) {
                        BaseModel model = ModelUtils.newInstanceOf(klass);
                        model.initWithValueMap(valueMap);
                        models.add(model);
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
    public boolean saveItem(BaseModel item) {
        SchemaInfo info = mClassNamesWithSchemaInfo.get(item.getClass().getSimpleName());
        ContentValues values = item.toValueMap();
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        String where = new WhereClause()
                .equalTo(info.getNameOfPrimaryKeyField(), item.getPrimaryKey())
                .toStatement();

        // Update if the specified item exists in the database.
        int affectedRows;
        db.beginTransaction();
        try {
            affectedRows = db.update(info.getModelName(), values, where, null);
            if (affectedRows == 1) {
                db.setTransactionSuccessful();
            }

        } finally {
            db.endTransaction();
        }

        if (affectedRows == 1) {
            db.close();
            return true;
        } else if (1 < affectedRows) {
            // Should not be reached
            throw new IllegalStateException(
                    "the id of the specified item is not unique");
        }

        // If not, insert it as a new item to the database.
        values.remove(info.getNameOfPrimaryKeyField());
        long newRowId = db.insert(info.getModelName(), null, values);
        db.close();

        if (newRowId != -1) {
            item.setPrimaryKey(newRowId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteItem(BaseModel item) {
        SchemaInfo info = mClassNamesWithSchemaInfo.get(item.getClass().getSimpleName());
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        String where = new WhereClause()
                .equalTo(info.getNameOfPrimaryKeyField(), item.getPrimaryKey())
                .toStatement();

        int affectedRow;
        db.beginTransaction();
        try {
            affectedRow = db.delete(info.getModelName(), where, null);
            if (affectedRow == 1) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }

        if (affectedRow == 0) {
            return false;
        } else if (affectedRow == 1) {
            return true;
        } else {
            // Should not be reached
            throw new IllegalStateException(
                    "the id of the specified item is not unique");
        }
    }
}
