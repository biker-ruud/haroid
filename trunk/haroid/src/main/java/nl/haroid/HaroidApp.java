package nl.haroid;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class HaroidApp extends Application {

    public static final String PREF_KEY_MAX_TEGOED = "pref_max_tegoed";
    public static final String PREF_KEY_START_TEGOED = "pref_start_tegoed";
    public static final String PREF_KEY_USERNAME = "pref_username";
    public static final String PREF_KEY_PASSWORD = "pref_password";

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
