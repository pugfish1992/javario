package com.pugfish1992.javario.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pugfish1992.javario.SchemaInfo;
import com.pugfish1992.javario.datasource.FieldType;
import com.pugfish1992.sqliteutils.library.Column;
import com.pugfish1992.sqliteutils.library.DataType;
import com.pugfish1992.sqliteutils.library.TableCreator;
import com.pugfish1992.sqliteutils.library.TableUtils;

import java.util.List;

/**
 * Created by daichi on 10/28/17.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "foo.db";
    private static final int DATABASE_VERSION = 1;

    private List<SchemaInfo> mSchemaInfoList;

    public DbOpenHelper(Context context, List<SchemaInfo> schemaInfoList) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        mSchemaInfoList = schemaInfoList;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (SchemaInfo info : mSchemaInfoList) {
            TableCreator creator = TableCreator
                    .tableName(info.getModelName())
                    .addColumn(Column.nameAndDataType(info.getNameOfPrimaryKeyField(), DataType.INTEGER).isPrimaryKey(true));

            for (String fieldName : info.fieldNameSetWithoutPrimaryKey()) {
                DataType type = toSqliteDataTypeFromJavaDataType(info.getFieldTypeOf(fieldName));
                creator.addColumn(Column.nameAndDataType(fieldName, type));
            }

            creator.create(sqLiteDatabase);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for (SchemaInfo info : mSchemaInfoList) {
            TableUtils.deleteTableIfExists(info.getModelName(), sqLiteDatabase);
        }
        onCreate(sqLiteDatabase);
    }

    private DataType toSqliteDataTypeFromJavaDataType(FieldType fieldType) {
        switch (fieldType) {
            case INT:
            case LONG:
            case BOOLEAN: return DataType.INTEGER;
            case STRING: return DataType.TEXT;
            default:
                // Should not be reached
                throw new IllegalStateException();
        }
    }
}
