package nl.haroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter to load and save data to a database.
 *
 * @author Ruud de Jong
 */
public final class DBAdapter {
    private static final String LOG_TAG = "DBAdapter";

    private static final String KEY_ID = "ID";
    private static final String KEY_DATE_CODE = "DATE_CODE";
    private static final String KEY_AMOUNT = "AMOUNT";
    private static final String KEY_BUNDLE = "BUNDLE";
    private static final String TABLE_NAME = "balance";
    private static final String CONSTRAINT_NAME = "DATE_BUNDLE";
    private static final String DATABASE_NAME = "Balance";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "create table " + TABLE_NAME + " (" + KEY_ID + " integer primary key autoincrement, " +
            KEY_DATE_CODE + " integer not null, " +
            KEY_AMOUNT + " integer not null, " +
            KEY_BUNDLE + " text not null," +
            "CONSTRAINT " + CONSTRAINT_NAME + " UNIQUE (" + KEY_DATE_CODE + ", " + KEY_BUNDLE + ")); " +
            "create index BUNDLE_DATE_IDX on balance (" + KEY_DATE_CODE + ", " + KEY_BUNDLE + ");";

    private DBHelper dbHelper;

    public DBAdapter(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public void saveOrUpdate(Date datum, int tegoed, BundleType bundleType) {
        int datumCode = Utils.bepaalDatumCode(datum);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(KEY_AMOUNT, String.valueOf(tegoed));
            String whereClause = KEY_DATE_CODE + "=? AND " + KEY_BUNDLE + "=?";
            long updatedRecords = database.update(TABLE_NAME, updateValues, whereClause, new String[]{String.valueOf(datumCode), bundleType.name()});
            if (updatedRecords != 1) {
                Log.d(LOG_TAG, "saveOrUpdate(): Update failed, trying to insert...");
                ContentValues insertValues = new ContentValues();
                insertValues.put(KEY_DATE_CODE, String.valueOf(datumCode));
                insertValues.put(KEY_AMOUNT, String.valueOf(tegoed));
                insertValues.put(KEY_BUNDLE, bundleType.name());
                long result = database.insert(TABLE_NAME, null, insertValues);
                if (result == -1) {
                    Log.d(LOG_TAG, "saveOrUpdate(): Insert failed.");
                }
            }
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "saveOrUpdate(): Runtime exception while updating database.");
        } finally {
            database.close();
        }
    }

    public int getBalance(Date datum, BundleType bundleType) {
        int balance = -1;
        int datumCode = Utils.bepaalDatumCode(datum);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try {
            Cursor result = database.query(TABLE_NAME, new String[]{KEY_AMOUNT}, KEY_DATE_CODE + "=? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(datumCode), bundleType.name()}, null, null, KEY_DATE_CODE + " DESC");
            if (result.getCount() >= 1) {
                result.moveToFirst();
                balance = result.getInt(0);
            }
            result.close();
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "getBalance(): Runtime exception while selecting database.");
        } finally {
            database.close();
        }
        return balance;
    }

    public int getMostRecentBalance(Date afterDate, Date beforeDate, BundleType bundleType) {
        int balance = -1;
        int afterDatumCode = Utils.bepaalDatumCode(afterDate);
        int beforeDatumCode = Utils.bepaalDatumCode(beforeDate);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try {
            Cursor result = database.query(TABLE_NAME, new String[]{KEY_AMOUNT}, KEY_DATE_CODE + ">? AND " + KEY_DATE_CODE + "<? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(afterDatumCode), String.valueOf(beforeDatumCode), bundleType.name()}, null, null, null);
            if (result.getCount() == 1) {
                result.moveToFirst();
                balance = result.getInt(0);
            }
            result.close();
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "getBalance(): Runtime exception while selecting database.");
        } finally {
            database.close();
        }
        return balance;
    }

    public Map<Integer, Integer> getBalanceList(Date afterDate, BundleType bundleType) {
        int afterDatumCode = Utils.bepaalDatumCode(afterDate);
        Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try {
            Cursor result = database.query(TABLE_NAME, new String[]{KEY_DATE_CODE, KEY_AMOUNT}, KEY_DATE_CODE+ ">? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(afterDatumCode), bundleType.name()}, null, null, null);
            while (result.moveToNext()) {
                int dateCode = result.getInt(0);
                int amount = result.getInt(1);
                resultMap.put(dateCode, amount);
            }
            result.close();
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "getBalanceList(): Runtime exception while selecting database.");
        } finally {
            database.close();
        }
        return resultMap;
    }

    public void reset() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            dbHelper.reset(database);
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "reset(): Runtime exception while resetting database.");
        } finally {
            database.close();
        }
    }

    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "onCreate()");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(LOG_TAG, "onUpgrade() "  + oldVersion + " -> " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        public void reset(SQLiteDatabase db) {
            Log.d(LOG_TAG, "reset()");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
