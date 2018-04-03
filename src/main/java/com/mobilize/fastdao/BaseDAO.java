package com.mobilize.fastdao;

/**
 * Created by o-emorales on 13/09/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by o-emorales on 06/09/2017.
 */

public abstract class BaseDAO<T extends DbModel>{
    private static final String TAG = "FastDAO";
    protected final SQLiteDatabase mSqlLiteDatabase;
    protected final Context context;
    protected final Uri contentURI;

    public BaseDAO(Context context, SQLiteDatabase sqlLiteDatabase, Uri contentURI) {
        this.context = context;
        this.mSqlLiteDatabase = sqlLiteDatabase;
        this.contentURI = contentURI;
    }

    public abstract String getTableName();

    public abstract T fromCursor(Cursor c);

    public abstract ContentValues values(T t);

    public boolean isNotEmpty() {
        Cursor c = null;
        try {
            if (mSqlLiteDatabase == null) {
                Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
                c = context.getContentResolver().query(contentUri, null, null, null, null);
            } else {
                c = mSqlLiteDatabase.rawQuery("SELECT * FROM " + getTableName(), null);
            }
            return c.moveToFirst();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public T findByPrimaryKey(String pkColName, long id) {
        //public T findByPrimaryKey(long id) {
        Cursor c = null;
        T t = null;

        try {
            if (mSqlLiteDatabase == null) {
                Uri contentUri = Uri.withAppendedPath(contentURI, getTableName() + "/" + id);
                c = context.getContentResolver().query(contentUri, null, null, null, null);
            } else {
                c = mSqlLiteDatabase.rawQuery("SELECT * FROM " + getTableName() + " WHERE " + pkColName
                        + " = ?", whereArgsForId(id));
            }
            Log.v(TAG, "cursor: " + DatabaseUtils.dumpCursorToString(c));
            if (c.moveToFirst()) {
                t = fromCursor(c);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return t;
    }

    public T findFirstByField(String fieldName, String value) {
        Cursor c = null;
        T t = null;
        String WHERE;
        String[] projection = null;
        try {
            if (mSqlLiteDatabase == null) {
                WHERE = fieldName + " = ?";
                String[] WHERE_ARGS = new String[]{value};
                Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
                c = context.getContentResolver().query(contentUri, projection, WHERE, WHERE_ARGS, null);
            } else {
                String q = "SELECT * FROM " + getTableName() + " WHERE " + fieldName + " = ?";
                c = mSqlLiteDatabase.rawQuery(q, new String[]{value});
            }
            Log.v(TAG, "cursor: " + getTableName() + " -> " + DatabaseUtils.dumpCursorToString(c));

            if (c.moveToFirst()) {
                t = fromCursor(c);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return t;
    }

    public List<T> selectAll() {
        Cursor c = null;
        try {
            if (mSqlLiteDatabase == null) {
                Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
                c = context.getContentResolver().query(contentUri, null, null, null, null);
            } else {
                c = mSqlLiteDatabase.rawQuery("SELECT * FROM " + getTableName(), null);
            }
            return allFromCursor(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public List<T> findAllByField(String fieldName, String value) {
        Cursor c = null;
        List<T> result = null;
        String WHERE;
        String[] projection = null;
        try {
            if (mSqlLiteDatabase == null) {
                WHERE = fieldName + " = ?";
                String[] WHERE_ARGS = new String[]{value};
                Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
                c = context.getContentResolver().query(contentUri, projection, WHERE, WHERE_ARGS, null);
            } else {
                c = mSqlLiteDatabase.rawQuery("SELECT * FROM " + getTableName() + " WHERE " + fieldName + " = ? ", new String[]{value});
            }
            Log.v(TAG, "cursor: " + getTableName() + " -> " + DatabaseUtils.dumpCursorToString(c));
            return allFromCursor(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public long create(T model) {
        long result = -1;
        if (mSqlLiteDatabase == null) {
            Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
            Uri resultUri = context.getContentResolver().insert(contentUri, values(model));
            context.getContentResolver().notifyChange(contentUri, null);
            result = Long.valueOf(resultUri.getPathSegments().get(0));
        } else {
            result = (int) mSqlLiteDatabase.insert(getTableName(),
                    null,
                    values(model));
        }

        Log.d(TAG, "NEW RECORD ID: " + result);
        return result;

    }

    public int update(T model, String pkColName, long pkColVal) {
        int result = -1;
        if (mSqlLiteDatabase == null) {
            Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
            context.getContentResolver().update(contentUri, values(model), pkColName + " = ?", whereArgsForId(pkColVal));
        } else {
            result = mSqlLiteDatabase.update(getTableName(), values(model), pkColName + " = ?", whereArgsForId(pkColVal));
        }
        return result;
    }

    public void delete(String pkColName, long pkColValue) {

        if (mSqlLiteDatabase == null) {
            String WHERE = pkColName + " = ?";
            String[] WHERE_ARGS = new String[]{"" + pkColValue};
            Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
            context.getContentResolver().delete(contentUri, pkColName + " = ?", whereArgsForId(pkColValue));
        } else {
            mSqlLiteDatabase.delete(getTableName(), pkColName + " = ?", whereArgsForId(pkColValue));
        }
    }

    public void deleteAll() {
        if (mSqlLiteDatabase == null) {
            Uri contentUri = Uri.withAppendedPath(contentURI, getTableName());
            Integer result = context.getContentResolver().delete(contentUri, null, null);
        } else {
            mSqlLiteDatabase.delete(getTableName(), null, null);
        }
    }

    public boolean exists(String pkColName, long pkColValue) {
        Cursor c = null;

        try {
            c = mSqlLiteDatabase.rawQuery("select _id from " + getTableName() + " WHERE " + pkColName + " = ?", whereArgsForId(pkColValue));
            return c.moveToFirst();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public List<T> findAll() {
        Cursor c = null;
        try {
            c = mSqlLiteDatabase.rawQuery("SELECT * FROM " + getTableName(), null);
            return allFromCursor(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    protected String[] whereArgsForId(long id) {
        return new String[]{String.valueOf(id)};
    }

    protected String arrayToCommaSeparatedValues(int[] ids) {
        StringBuilder sqlFragment = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            sqlFragment.append(ids[i]);
            if (i < ids.length - 1) {
                sqlFragment.append(',');
            }
        }
        return sqlFragment.toString();
    }

    protected List<T> allFromCursor(Cursor cursor) {
        if (cursor.moveToFirst()) {
            List<T> result = new ArrayList<T>();
            do {
                result.add(fromCursor(cursor));
            } while (cursor.moveToNext());
            return result;
        }
        return Collections.emptyList();
    }
}
