package com.pugfish1992.sqliteutils.library;

import android.support.annotation.NonNull;

/**
 * Created by daichi on 10/29/17.
 *
 * Use this class with {@link WhereClause}
 */

public final class Column {

    @NonNull
    private String mName;
    @NonNull private DataType mDataType;
    private boolean mIsPrimaryKey;
    private boolean mIsNotNull;
    private Object mDefaultValue;

    public static Column nameAndDataType(
            @NonNull String name, @NonNull DataType dataType) {

        return new Column(name, dataType);
    }

    public Column(@NonNull String name, @NonNull DataType dataType) {
        mName = name;
        mDataType = dataType;
        mIsPrimaryKey = false;
        mIsNotNull = false;
        mDefaultValue = null;
    }

    public Column name(@NonNull String name) {
        mName = name;
        return this;
    }

    public Column dataType(DataType dataType) {
        mDataType = dataType;
        return this;
    }

    public Column isPrimaryKey(boolean isPrimaryKey) {
        mIsPrimaryKey = isPrimaryKey;
        return this;
    }

    public Column isNotNull(boolean isNotNull) {
        mIsNotNull = isNotNull;
        return this;
    }

    public Column defaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
        return this;
    }

    /* Intentional package-private */
    @NonNull
    String getName() {
        return mName;
    }

    /* Intentional package-private */
    String toStatement() {
        String statement = mName + " " + mDataType.name();

        if (mIsPrimaryKey) {
            statement += " primary key";
        }
        if (mIsNotNull) {
            statement += " not null";
        }
        if (mDefaultValue != null) {
            statement += " default ";
            if (mDefaultValue instanceof CharSequence) {
                statement += "'" + mDefaultValue.toString() + "'";
            } else {
                statement += mDefaultValue.toString();
            }
        }

        return statement;
    }
}