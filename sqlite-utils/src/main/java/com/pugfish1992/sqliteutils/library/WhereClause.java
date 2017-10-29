package com.pugfish1992.sqliteutils.library;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Locale;

/*
// USAGE

        WhereClause whereClause = new WhereClause();
        String tag = WhereClauseUsage.class.getSimpleName();

        whereClause
                .equalTo("col1", 5)
                .AND()
                .notEqualTo("col2", "abc")
                .OR()
                .between("col3", -123, 777);
        Log.d(tag, "where -> " + whereClause.toStatement());
        whereClause.clear();

        whereClause
                .in("col1", 1,2,3,4,5,6)
                .OR()
                .beginParentheses()
                .like("col2", "%banana%")
                .AND()
                .greaterThanOrEqualTo("col3", 6)
                .AND()
                .notIn("col3", "a", "b", "c")
                .AND()
                .isNotNull("col4")
                .endParentheses();
        Log.d(tag, "where -> " + whereClause.toStatement());
        whereClause.clear();

        whereClause
                .between("col1", 10, 100, false, true)
                .OR()
                .isNull("col2");
        Log.d(tag, "where -> " + whereClause.toStatement());
        whereClause.clear();

        whereClause
                .notBetween("col1", 10, 100)
                .AND()
                .beginParentheses()
                .notLike("col2", "%foo%")
                .OR()
                .notBetween("col3", 10, 100)
                .endParentheses();
        Log.d(tag, "where -> " + whereClause.toStatement());

 */

public final class WhereClause {

    private String mStatement;

    public String toStatement() {
        return mStatement;
    }

    public void clear() {
        mStatement = null;
    }

    public WhereClause beginParentheses() {
        appendToken("(");
        return this;
    }

    public WhereClause endParentheses() {
        appendToken(")");
        return this;
    }

    /**
     * SQLITE COMPARISON OPERATORS
     * ---------- */

    public WhereClause equalTo(@NonNull String column, Object value) {
        if (value == null) {
            String exp = format("%s is null", column);
            appendToken(exp);
        } else {
            appendComparisonExpression(column, value, "=");
        }

        return this;
    }

    public WhereClause notEqualTo(@NonNull String column, Object value) {
        if (value == null) {
            String exp = format("%s is not null", column);
            appendToken(exp);
        } else {
            appendComparisonExpression(column, value, "!=");
        }

        return this;
    }

    public WhereClause lessThan(@NonNull String column, @NonNull Object value) {
        appendComparisonExpression(column, value, "<");
        return this;
    }

    public WhereClause greaterThan(@NonNull String column, @NonNull Object value) {
        appendComparisonExpression(column, value, ">");
        return this;
    }

    public WhereClause lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        appendComparisonExpression(column, value, "<=");
        return this;
    }

    public WhereClause greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        appendComparisonExpression(column, value, ">=");
        return this;
    }

    public WhereClause isNull(@NonNull String column) {
        return equalTo(column, null);
    }

    public WhereClause isNotNull(@NonNull String column) {
        return notEqualTo(column, null);
    }

    /**
     * SQLITE LOGICAL OPERATIONS
     * ---------- */

    public WhereClause AND() {
        appendToken("and");
        return this;
    }

    public WhereClause OR() {
        appendToken("or");
        return this;
    }

    public WhereClause NOT() {
        appendToken("not");
        return this;
    }

    public WhereClause between(@NonNull String column, @NonNull Object min, @NonNull Object max) {
        return between(column, min, max, true, true, false);
    }

    public WhereClause between(@NonNull String column, @NonNull Object min, @NonNull Object max,
                               boolean includeMin, boolean includeMax) {

        return between(column, min, max, includeMin, includeMax, false);
    }

    public WhereClause notBetween(@NonNull String column, @NonNull Object min, @NonNull Object max) {
        return between(column, min, max, true, true, true);
    }

    public WhereClause notBetween(@NonNull String column, @NonNull Object min, @NonNull Object max,
                               boolean includeMin, boolean includeMax) {

        return between(column, min, max, includeMin, includeMax, true);
    }

    private WhereClause between(@NonNull String column, @NonNull Object min, @NonNull Object max,
                               boolean includeMin, boolean includeMax, boolean isNot) {

        if (includeMin && includeMax) {
            String minStr = (min instanceof CharSequence) ? format("'%s'", min.toString()) : min.toString();
            String maxStr = (max instanceof CharSequence) ? format("'%s'", max.toString()) : max.toString();
            String exp;
            if (isNot) {
                exp = format("%s not between %s and %s", column, minStr, maxStr);
            } else {
                exp = format("%s between %s and %s", column, minStr, maxStr);
            }

            appendToken(exp);
            return this;
        }

        if (isNot) {
            if (includeMin) {
                lessThan(column, min);
            } else {
                lessThanOrEqualTo(column, min);
            }

            OR();

            if (includeMax) {
                greaterThan(column, max);
            } else {
                greaterThanOrEqualTo(column, max);
            }

        } else {
            if (includeMin) {
                greaterThanOrEqualTo(column, min);
            } else {
                greaterThan(column, min);
            }

            AND();

            if (includeMax) {
                lessThanOrEqualTo(column, max);
            } else {
                lessThan(column, max);
            }
        }

        return this;
    }

    public WhereClause in(@NonNull String column, @NonNull Object... values) {
        return in(column, false, values);
    }

    public WhereClause notIn(@NonNull String column, @NonNull Object... values) {
        return in(column, true, values);
    }

    private WhereClause in(@NonNull String column, boolean isNot, @NonNull Object... values) {
        if (values.length == 0) return this;
        if (values[0] instanceof CharSequence) {
            for (int i = 0; i < values.length; ++i) {
                values[i] = format("'%s'", values[i]);
            }
        }

        String exp;
        if (isNot) {
            exp = format("%s not in (%s)", column, TextUtils.join(",", values));
        } else {
            exp = format("%s in (%s)", column, TextUtils.join(",", values));
        }
        appendToken(exp);
        return this;
    }

    public WhereClause like(@NonNull String column, @NonNull String regex) {
        return like(column, regex, false);
    }

    public WhereClause notLike(@NonNull String column, @NonNull String regex) {
        return like(column, regex, true);
    }

    private WhereClause like(@NonNull String column, @NonNull String regex, boolean isNot) {
        String exp;
        if (isNot) {
            exp = format("%s not like '%s'", column, regex);
        } else {
            exp = format("%s like '%s'", column, regex);
        }
        appendToken(exp);
        return this;
    }

    /**
     * UTILS
     * ---------- */

    private void appendComparisonExpression(
            @NonNull String column, @NonNull Object value, @NonNull String compOperator) {

        String exp;
        if (value instanceof CharSequence) {
            exp = String.format(Locale.US, "%s %s '%s'", column, compOperator, value.toString());
        } else {
            exp = String.format(Locale.US, "%s %s %s", column, compOperator, value.toString());
        }
        appendToken(exp);
    }

    private void appendToken(@NonNull String token) {
        if (mStatement == null) {
            mStatement = token;
        } else {
            mStatement += " " + token;
        }
    }

    private String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
