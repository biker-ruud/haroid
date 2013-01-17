package nl.haroid.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ruud de Jong
 */
public final class StorageOpenHelperSqliteImpl implements StorageOpenHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageOpenHelperSqliteImpl.class);

    private final int databaseVersion;
    private CreateListener createListener;
    private UpgradeListener upgradeListener;
    private StorageSqliteImpl database;

    public StorageOpenHelperSqliteImpl(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

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
        return createOrOpenDatabase(false);
    }

    @Override
    public Storage getWritableStorage() {
        return createOrOpenDatabase(true);
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    private StorageSqliteImpl createOrOpenDatabase(boolean writable) {
        if (database == null) {
            database = new StorageSqliteImpl();
        }
        database.open();
        final int version = database.getVersion();
        if (version != this.databaseVersion) {
            if (!writable) {
//            if (database.isReadOnly()) {
                LOGGER.warn("Can't upgrade read-only database from version " +
                        database.getVersion() + " to " + this.databaseVersion);
            }

            if (version == 0) {
                LOGGER.info("Database has no version. Need to create database first.");
                onCreate(database);
            } else {
                if (version > this.databaseVersion) {
//                    onDowngrade(database, version, this.databaseVersion);
                    LOGGER.error("Downgrading databases is not implemented.");
                    throw new IllegalArgumentException("Downgrading databases is not implemented.");
                } else {
                    onUpgrade(database, version, this.databaseVersion);
                }
            }
            database.setVersion(this.databaseVersion);
        }

        return database;
    }

    private void onCreate(Storage storage) {
        if (this.createListener != null) {
            this.createListener.onCreate(storage);
        }
    }

    private void onUpgrade(Storage storage, int oldVersion, int newVersion) {
        if (this.upgradeListener != null) {
            this.upgradeListener.onUpgrade(storage, oldVersion, newVersion);
        }
    }
}
