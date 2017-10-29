package com.pugfish1992.sqliteutils.library;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daichi on 10/29/17.
 */

public class Query {

    private static final boolean DEFAULT_DISTINCT = false;

    private boolean mDistinct;
    @NonNull private List<String> mTables;
    @NonNull private List<String> mProjection;
    private String mSelection;
    @NonNull private List<String> mSelectionArgs;

    public Query() {
        mDistinct = DEFAULT_DISTINCT;
        mTables = new ArrayList<>();
        mProjection = new ArrayList<>();
        mSelection = null;
        mSelectionArgs = new ArrayList<>();
    }

    public Cursor startQuery(@NonNull SQLiteDatabase db) {
        return db.query(
                mDistinct,
                (mTables.size() != 0) ? TextUtils.join(" join ", mTables) : null,
                (mProjection.size() != 0) ? mProjection.toArray(new String[]{}) : null,
                mSelection,
                (mSelectionArgs.size() != 0) ? mSelectionArgs.toArray(new String[]{}) : null,
                null, // group by
                null, // having
                null, //order by
                null); // limit
    }

    public void startQueryAndScanResult(@NonNull SQLiteDatabase db, @NonNull ScanningResultCallback callback) {
        Cursor cursor = startQuery(db);
        boolean hasNext = cursor.moveToFirst();

        callback.onStartScanning(cursor);
        while (hasNext) {
            ContentValues valueMap = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, valueMap);
            callback.onScanRow(valueMap);
            hasNext = cursor.moveToNext();
        }

        if (callback.onEndScanning(cursor)) {
            cursor.close();
        }
    }

    public Query setDistinct(boolean distinct) {
        mDistinct = distinct;
        return this;
    }

    public Query setTables(@NonNull List<String> tables) {
        mTables = tables;
        return this;
    }

    public Query addTables(@NonNull String... tables) {
        Collections.addAll(mTables, tables);
        return this;
    }

    public Query setProjection(@NonNull List<String> projection) {
        mProjection = projection;
        return this;
    }

    public Query addProjection(@NonNull String... projection) {
        Collections.addAll(mProjection, projection);
        return this;
    }

    public Query setSelection(String selection) {
        mSelection = selection;
        return this;
    }

    public Query setSelectionArgs(@NonNull List<String> selectionArgs) {
        mSelectionArgs = selectionArgs;
        return this;
    }

    public Query addSelectionArgs(@NonNull String... selectionArgs) {
        Collections.addAll(mSelectionArgs, selectionArgs);
        return this;
    }

    /* ------------------------------------------------------------------------------- *
     * RESULT LISTENER INTERFACE
     * ------------------------------------------------------------------------------- */

    public interface ScanningResultCallback {
        void onStartScanning(Cursor cursor);
        void onScanRow(ContentValues valueMap);
        // Return false if you want to close a cursor manually.
        boolean onEndScanning(Cursor cursor);
    }
}
