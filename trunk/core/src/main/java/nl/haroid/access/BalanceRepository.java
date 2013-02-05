package nl.haroid.access;

import nl.haroid.common.BundleType;

import java.util.Date;
import java.util.Map;

/**
 * Interface to load and save data to a persistent layer.
 *
 * @author Ruud de Jong
 */
public interface BalanceRepository {

    /**
     * Save or update the amount for a given date and bundletype
     * @param datum the date.
     * @param tegoed the amount.
     * @param bundleType type of bundle.
     */
    void saveOrUpdate(Date datum, int tegoed, BundleType bundleType);

    /**
     * Gets the balance for a given date and bundle type.
     * @param datum the date.
     * @param bundleType the type of bundle.
     * @return the balance.
     */
    int getBalance(Date datum, BundleType bundleType);

    /**
     * Gets the most recent balance between given dates.
     * @param afterDate balance must be at or after this date.
     * @param beforeDate balance must be at or before this date.
     * @param bundleType type of bundle.
     * @return the balance ot '-1' if not found.
     */
    int getMostRecentBalance(Date afterDate, Date beforeDate, BundleType bundleType);

    /**
     * Gets a map of date / balance for given bundle type.
     * @param afterDate dates must be at or after this date.
     * @param bundleType type of bundle.
     * @return the map of <date, balance>.
     */
    Map<Integer, Integer> getBalanceList(Date afterDate, BundleType bundleType);

    /**
     * Resets the persistency. Wipes all data.
     */
    void reset();
}
