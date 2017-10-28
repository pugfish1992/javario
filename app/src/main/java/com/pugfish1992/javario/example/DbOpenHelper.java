package com.pugfish1992.javario.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pugfish1992.javario.SchemaInfo;

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
        if (mSchemaInfoList == null) return;


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
