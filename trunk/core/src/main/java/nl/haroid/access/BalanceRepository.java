package nl.haroid.access;

import nl.haroid.common.BundleType;
import nl.haroid.common.Utils;
import nl.haroid.storage.CreateListener;
import nl.haroid.storage.Storage;
import nl.haroid.storage.StorageOpenHelper;
import nl.haroid.storage.UpgradeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to load and save data to a database.
 *
 * @author Ruud de Jong
 */
public final class BalanceRepository {
    public static final String DATABASE_NAME = "Balance";
    public static final int DATABASE_VERSION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceRepository.class);
    private static final String KEY_ID = "ID";
    private static final String KEY_DATE_CODE = "DATE_CODE";
    private static final String KEY_AMOUNT = "AMOUNT";
    private static final String KEY_BUNDLE = "BUNDLE";
    private static final String TABLE_NAME = "balance";
    private static final String CONSTRAINT_NAME = "DATE_BUNDLE";
    private static final String DATABASE_CREATE = "create table " + TABLE_NAME + " (" + KEY_ID + " integer primary key autoincrement, " +
            KEY_DATE_CODE + " integer not null, " +
            KEY_AMOUNT + " integer not null, " +
            KEY_BUNDLE + " text not null," +
            "CONSTRAINT " + CONSTRAINT_NAME + " UNIQUE (" + KEY_DATE_CODE + ", " + KEY_BUNDLE + ")); " +
            "create index BUNDLE_DATE_IDX on balance (" + KEY_DATE_CODE + ", " + KEY_BUNDLE + ");";

    private StorageOpenHelper storageOpenHelper;

    public BalanceRepository(StorageOpenHelper storageOpenHelper) {
        this.storageOpenHelper = storageOpenHelper;
        this.storageOpenHelper.registerOnCreateListener(new CreateListener() {
            @Override
            public void onCreate(Storage storage) {
                create(storage);
            }
        });
        this.storageOpenHelper.registerOnUpgradeListener(new UpgradeListener() {
            @Override
            public void onUpgrade(Storage storage, int oldVersion, int newVersion) {
                upgrade(storage, oldVersion, newVersion);
            }
        });
    }

    public void saveOrUpdate(Date datum, int tegoed, BundleType bundleType) {
        int datumCode = Utils.bepaalDatumCode(datum);
        Storage database = storageOpenHelper.getWritableStorage();
        try {
            Map<String, String> updateValues = new HashMap<String, String>();
            updateValues.put(KEY_AMOUNT, String.valueOf(tegoed));
            String whereClause = KEY_DATE_CODE + "=? AND " + KEY_BUNDLE + "=?";
            long updatedRecords = database.update(TABLE_NAME, updateValues, whereClause, new String[]{String.valueOf(datumCode), bundleType.name()});
            if (updatedRecords != 1) {
                LOGGER.info("saveOrUpdate(): Update failed, trying to insert...");
                Map<String, String> insertValues = new HashMap<String, String>();
                insertValues.put(KEY_DATE_CODE, String.valueOf(datumCode));
                insertValues.put(KEY_AMOUNT, String.valueOf(tegoed));
                insertValues.put(KEY_BUNDLE, bundleType.name());
                long result = database.insert(TABLE_NAME, null, insertValues);
                if (result == -1) {
                    LOGGER.info("saveOrUpdate(): Insert failed.");
                }
            }
        } catch (RuntimeException e) {
            LOGGER.warn("saveOrUpdate(): Runtime exception while updating database.");
        } finally {
            storageOpenHelper.close();
        }
    }

    public int getBalance(Date datum, BundleType bundleType) {
        int balance = -1;
        int datumCode = Utils.bepaalDatumCode(datum);
        Storage database = storageOpenHelper.getReadableStorage();
        try {
            String result = database.queryUniqueResult(TABLE_NAME, new String[]{KEY_AMOUNT}, KEY_DATE_CODE + "=? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(datumCode), bundleType.name()}, null, null, KEY_DATE_CODE + " DESC");
            if (result != null) {
                balance = Integer.parseInt(result);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("getBalance(): Runtime exception while selecting database.");
        } finally {
            storageOpenHelper.close();
        }
        return balance;
    }

    public int getMostRecentBalance(Date afterDate, Date beforeDate, BundleType bundleType) {
        int balance = -1;
        int afterDatumCode = Utils.bepaalDatumCode(afterDate);
        int beforeDatumCode = Utils.bepaalDatumCode(beforeDate);
        Storage database = storageOpenHelper.getReadableStorage();
        try {
            String result = database.queryUniqueResult(TABLE_NAME, new String[]{KEY_AMOUNT}, KEY_DATE_CODE + ">? AND " + KEY_DATE_CODE + "<? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(afterDatumCode), String.valueOf(beforeDatumCode), bundleType.name()}, null, null, null);
            if (result != null) {
                balance = Integer.parseInt(result);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("getBalance(): Runtime exception while selecting database.");
        } finally {
            storageOpenHelper.close();
        }
        return balance;
    }

    public Map<Integer, Integer> getBalanceList(Date afterDate, BundleType bundleType) {
        int afterDatumCode = Utils.bepaalDatumCode(afterDate);
        Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
        Storage database = storageOpenHelper.getReadableStorage();
        try {
            List<String[]> rawResultList = database.query(TABLE_NAME, new String[]{KEY_DATE_CODE, KEY_AMOUNT}, KEY_DATE_CODE+ ">? AND " + KEY_BUNDLE + "=?", new String[]{String.valueOf(afterDatumCode), bundleType.name()}, null, null, null);
            for (String[] resultRow : rawResultList) {
                int dateCode = Integer.parseInt(resultRow[0]);
                int amount = Integer.parseInt(resultRow[1]);
                resultMap.put(dateCode, amount);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("getBalanceList(): Runtime exception while selecting database.");
        } finally {
            storageOpenHelper.close();
        }
        return resultMap;
    }

    public void reset() {
        Storage database = storageOpenHelper.getWritableStorage();
        try {
            reset(database);
        } catch (RuntimeException e) {
            LOGGER.warn("reset(): Runtime exception while resetting database.");
        } finally {
            storageOpenHelper.close();
        }
    }

    // Database restructure methods
    private void create(Storage storage) {
        LOGGER.info("onCreate()");
        storage.execSQL(DATABASE_CREATE);
    }

    private void upgrade(Storage storage, int oldVersion, int newVersion) {
        LOGGER.info("onUpgrade() "  + oldVersion + " -> " + newVersion);
        storage.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        create(storage);
    }

    private void reset(Storage storage) {
        LOGGER.info("reset()");
        storage.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        create(storage);
    }

}
