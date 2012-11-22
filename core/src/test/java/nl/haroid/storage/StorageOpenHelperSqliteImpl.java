package nl.haroid.storage;

/**
 * @author Ruud de Jong
 */
public final class StorageOpenHelperSqliteImpl implements StorageOpenHelper {

    private CreateListener createListener;
    private UpgradeListener upgradeListener;
    private StorageSqliteImpl database;

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
        return createOrOpenDatabase();
    }

    @Override
    public Storage getWritableStorage() {
        return createOrOpenDatabase();
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    private StorageSqliteImpl createOrOpenDatabase() {
        if (database == null) {
            database = new StorageSqliteImpl();
        }
        database.open();
        return database;
    }
}
