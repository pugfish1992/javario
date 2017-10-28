package com.pugfish1992.sqliteutils.library;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

/**
 * Created by daichi on 10/29/17.
 */

public class TableUtils {

    private TableUtils() {}

    public static void deleteTable(String table, SQLiteDatabase db) {
        if (table == null || db == null) return;
        db.execSQL("drop table " + table);
    }

    public static void deleteTableIfExists(String table, SQLiteDatabase db) {
        if (table == null || db == null) return;
        db.execSQL("drop table if exists " + table);
    }
}
