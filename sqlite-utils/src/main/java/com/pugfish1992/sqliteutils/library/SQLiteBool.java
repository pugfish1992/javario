package com.pugfish1992.sqliteutils.library;

/**
 * Created by daichi on 10/29/17.
 *
 * SQLite does not have a separate Boolean storage class. Instead,
 * Boolean values are stored as integers 0 (false) and 1 (true).
 * See more info -> http://www.sqlite.org/datatype3.html
 */

public final class SQLiteBool {

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    private boolean mValue;

    public SQLiteBool(boolean value) {
        mValue = value;
    }

    public SQLiteBool(int value) {
        mValue = toBoolean(value);
    }

    public int toInt() {
        return toInt(mValue);
    }

    public static int toInt(boolean value) {
        return (value) ? TRUE : FALSE;
    }

    /**
     * Return false if 'value' is 0, true otherwise.
     */
    public static boolean toBoolean(int value) {
        return value != 0;
    }
}
