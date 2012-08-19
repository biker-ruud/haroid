package nl.haroid;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class HaroidApp extends Application {

    public static final String PREF_KEY_MAX_TEGOED = "pref_max_tegoed";
    public static final String PREF_KEY_START_TEGOED = "pref_start_tegoed";
    public static final String PREF_KEY_USERNAME = "pref_username";
    public static final String PREF_KEY_PASSWORD = "pref_password";
    public static final String PREF_KEY_ENABLE_AUTO_UPDATE = "pref_enable_auto_update";
    public static final String PREF_KEY_UPDATE_CHANNEL = "pref_update_channel";
    public static final String PREF_KEY_WIFI_UPDATE_INTERVAL = "pref_wifi_update_interval";
    public static final String PREF_KEY_MOBILE_UPDATE_INTERVAL = "pref_mobile_update_interval";
    public static final String PREF_KEY_LASTEST_UPDATE = "pref_latest_update";

    public static final String ALARM_MANAGER_SET = "alarm manager set";

    private static final String LOG_TAG = "HaroidApp";
    private static final String SHARED_PREFERENCE_NAME = "nl.haroid_preferences";
    private static final int SHARED_PREFERENCE_MODE = MODE_PRIVATE;

    private static SharedPreferences sharedPreferences;
    private static HaroidApp INSTANCE;

    private HistoryMonitor historyMonitor;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "OnCreate()");

        if (sharedPreferences == null) {
            sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCE_NAME, SHARED_PREFERENCE_MODE);
        }
        this.historyMonitor = new HistoryMonitor(sharedPreferences);
        INSTANCE = this;
    }

    public static HaroidApp getInstance() {
        return INSTANCE;
    }

    public static String getEmailAdres() {
        return sharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    public static String getPassword() {
        return sharedPreferences.getString(PREF_KEY_PASSWORD, "");
    }

    public static long getLatestUpdate() {
        return sharedPreferences.getLong(PREF_KEY_LASTEST_UPDATE, 0l);
    }

    public static boolean isAutoUpdateEnabled() {
        return sharedPreferences.getBoolean(PREF_KEY_ENABLE_AUTO_UPDATE, false);
    }

    public static String getUpdateChannel() {
        return sharedPreferences.getString(PREF_KEY_UPDATE_CHANNEL, "");
    }

    public static int getWifiUpdateInterval() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_WIFI_UPDATE_INTERVAL, "24"));
    }

    public static int getMobileUpdateInterval() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_MOBILE_UPDATE_INTERVAL, "24"));
    }

    public static int getMaxTegoed() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0"));
    }

    public static int getStartTegoed() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"));
    }

    public List<HistoryMonitor.UsagePoint> getUsageList() {
        return this.historyMonitor.getUsageList();
    }

    public Stats recalculate() {
        Stats stats = new Stats();
        stats.maxBalance = Integer.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0"));
        stats.startBalance = Integer.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"));
        bepaalGeschiedenis(stats.startBalance);
        berekenDuurTegoed(stats);
        return stats;
    }

    public void setCurrentBalance(int balance) {
        int maxBalance = Integer.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0"));
        int startBalance = Integer.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"));
        if (balance >= 0 && maxBalance > 0) {
            int dagInPeriode = Utils.bepaaldDagInPeriode(startBalance);
            Log.i(LOG_TAG, "dagInPeriode: " + dagInPeriode);
            this.historyMonitor.setTegoed(dagInPeriode, balance);
            Date updateDate = new Date();
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            prefEditor.putLong(PREF_KEY_LASTEST_UPDATE, updateDate.getTime());
            prefEditor.commit();
        }
    }

    public int getCurrentBalance() {
        int startBalance = Integer.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"));
        if (startBalance > 0) {
            int dagInPeriode = Utils.bepaaldDagInPeriode(startBalance);
            return this.historyMonitor.getBalance(dagInPeriode);
        } else {
            return -1;
        }
    }

    public int getBalanceYesterday() {
        return this.historyMonitor.getTegoedGisteren(Integer.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0")));
    }

    public void resetHistory() {
        this.historyMonitor.resetHistory();
    }

    private void berekenDuurTegoed(Stats stats) {
        Log.i(LOG_TAG, "berekenDuurTegoed");
        if (stats.startBalance == 0) {
            // Niet ingesteld.
            return;
        }
        int maxPeriod = berekenMaxPeriod(stats.startBalance);
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        int verbruik = 0;
        if (huidigeDagVdMaand < stats.startBalance) {
            // Tegoed begonnen in vorige maand
            Log.i(LOG_TAG, "Tegoed begonnen in vorige maand");
            verbruik = (maxPeriod - stats.startBalance) + 1;
            verbruik += huidigeDagVdMaand;
        } else {
            // Tegoed begonnen in deze maand.
            Log.i(LOG_TAG, "Tegoed begonnen in deze maand");
            verbruik = (huidigeDagVdMaand - stats.startBalance) + 1;
        }
        int nogTeGaan = maxPeriod - verbruik;
        int currentPeriod = verbruik;
        Log.i(LOG_TAG, "currentPeriod: " + currentPeriod);
        Log.i(LOG_TAG, "maxPeriod: " + maxPeriod);
        stats.maxPeriod = maxPeriod;
        stats.daysToGoInPeriod = nogTeGaan;
        stats.maxPeriod = maxPeriod;
    }

    private int berekenMaxPeriod(int startTegoed) {
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        if (huidigeDagVdMaand < startTegoed) {
            // Tegoed begonnen in vorige maand
            Log.i(LOG_TAG, "Tegoed begonnen in vorige maand");
            return Utils.numberOfDaysPreviousMonth();
        } else {
            // Tegoed begonnen in deze maand.
            Log.i(LOG_TAG, "Tegoed begonnen in deze maand");
            return Utils.numberOfDaysThisMonth();
        }
    }

    private void bepaalGeschiedenis(int startTegoed) {
        if (startTegoed == 0) {
            // Niet ingesteld
            return;
        }
        int periodeNummer = Utils.bepaalPeriodeNummer(startTegoed);
        Log.i(LOG_TAG, "periodeNummer: " + periodeNummer);
        this.historyMonitor.setPeriodeNummer(periodeNummer);
    }

    class Stats {
        int startBalance;
        int currentBalance;
        int maxBalance;
        int dayInPeriod;
        int daysToGoInPeriod;
        int maxPeriod;
    }

}
