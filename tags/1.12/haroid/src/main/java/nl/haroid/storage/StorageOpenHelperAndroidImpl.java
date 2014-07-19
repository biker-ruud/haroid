package nl.haroid.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Android implementation of the StorageOpenHelper Interface.
 * This is a wrapper around Android's SQLiteOpenHelper
 *
 * @author Ruud de Jong
 */
public final class StorageOpenHelperAndroidImpl extends SQLiteOpenHelper implements StorageOpenHelper {

    private static final String LOG_TAG = "StorageOpenHelperAndroidImpl";

    private CreateListener createListener;
    private UpgradeListener upgradeListener;

    public StorageOpenHelperAndroidImpl(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    // SQLiteOpenHelper methods

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate()");
        if (this.createListener != null) {
            this.createListener.onCreate(new StorageAndroidImpl(db));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade() "  + oldVersion + " -> " + newVersion);
        if (this.upgradeListener != null) {
            this.upgradeListener.onUpgrade(new StorageAndroidImpl(db), oldVersion, newVersion);
        }
    }

    // StorageOpenHelper methods
    @Override
    public void registerOnCreateListener(CreateListener createListener) {
        this.createListener = createListener;
    }

    @Override
    public void registerOnUpgradeListener(UpgradeListener upgradeListener) {
        this.upgradeListener= upgradeListener;
    }

    @Override
    public Storage getReadableStorage() {
        return new StorageAndroidImpl(this.getReadableDatabase());
    }

    @Override
    public Storage getWritableStorage() {
        return new StorageAndroidImpl(this.getWritableDatabase());
    }

    @Override
    public void close() {
        super.close();
    }
}
