package nl.haroid.storage;

/**
 * Interface comparable to Android's SQLiteOpenHelper.onCreate()
 *
 * @author Ruud de Jong
 */
public interface CreateListener {

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param storage The database.
     */
    public abstract void onCreate(Storage storage);
}
