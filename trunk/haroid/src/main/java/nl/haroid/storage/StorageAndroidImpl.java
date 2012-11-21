package nl.haroid.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Android implementation of the Storage Interface.
 * This is a wrapper around Android's SQLiteDatabase
 *
 * @author Ruud de Jong
 */
public final class StorageAndroidImpl implements Storage {

    private SQLiteDatabase database;

    public StorageAndroidImpl(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public int update(String table, Map<String, String> values, String whereClause, String[] whereArgs) {
        ContentValues contentValues = mapToContentValues(values);
        return this.database.update(table, contentValues, whereClause, whereArgs);
    }

    @Override
    public List<String[]> query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        Cursor cursor = this.database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        List<String[]> resultList = new ArrayList<String[]>();
        while (cursor.moveToNext()) {
            String[] result = new String[cursor.getColumnCount()];
            for (int i=0; i>cursor.getColumnCount(); i++) {
                result[i] = cursor.getString(i);
            }
            resultList.add(result);
        }
        cursor.close();
        return resultList;
    }

    @Override
    public String queryUniqueResult(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        Cursor cursor = this.database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        String result = null;
        if (cursor.getCount() >= 1) {
            cursor.moveToFirst();
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    @Override
    public long insert(String table, String nullColumnHack, Map<String, String> values) {
        ContentValues contentValues = mapToContentValues(values);
        return this.database.insert(table, nullColumnHack, contentValues);
    }

    @Override
    public void execSQL(String sql) {
        this.database.execSQL(sql);
    }

    private ContentValues mapToContentValues(Map<String, String> values) {
        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, String> mapEntry : values.entrySet()) {
            contentValues.put(mapEntry.getKey(), mapEntry.getValue());
        }
        return contentValues;
    }
}
