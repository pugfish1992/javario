package com.pugfish1992.sqliteutils.library;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daichi on 10/29/17.
 */

public class TableCreator {

    @NonNull final private String mTableName;
    private final List<Column> mColumns;

    public static TableCreator tableName(@NonNull String tableName) {
        return new TableCreator(tableName);
    }

    public TableCreator(@NonNull String tableName) {
        mTableName = tableName;
        mColumns = new ArrayList<>();
    }

    public TableCreator addColumn(@NonNull Column column) {
        for (int i = 0; i < mColumns.size(); ++i) {
            if (mColumns.get(i).getName().equals(column.getName())) {
                mColumns.set(i, column);
                return this;
            }
        }

        mColumns.add(column);
        return this;
    }

    public void create(@NonNull SQLiteDatabase db) {
        create(db, false);
    }

    public void createIfNotExist(@NonNull SQLiteDatabase db) {
        create(db, true);
    }

    private void create(@NonNull SQLiteDatabase db, boolean createIfNotExist) {
        String[] subStatements = new String[mColumns.size()];
        for (int i = 0; i < mColumns.size(); ++i) {
            subStatements[i] = mColumns.get(i).toStatement();
        }

        String statement = (createIfNotExist)
                ? "create table if not exists"
                : "create table";
        statement += " " + mTableName + " (" + TextUtils.join(",", subStatements) + ");";

        db.execSQL(statement);
    }
}
