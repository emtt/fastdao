package com.mobilize.fastdao;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by emorales on 13/09/2017.
 */

public interface DAO<T> {

    public static final String ID = "id";

    public T findByPrimaryKey(long id);
    public long create(T object);
    public void update(T object);
    public void createOrUpdate(T object);
    public void delete(long id);
    public boolean exists(long id);

    public T fromCursor(Cursor c);
    public ContentValues values(T t);
}
