package nl.haroid;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import nl.haroid.access.BalanceRepository;
import nl.haroid.access.BalanceRepositoryImpl;
import nl.haroid.common.Provider;
import nl.haroid.common.Theme;
import nl.haroid.common.Utils;
import nl.haroid.service.HistoryMonitor;
import nl.haroid.storage.StorageOpenHelperAndroidImpl;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public final class HaroidApp extends Application {

    public static final String PREF_KEY_PROVIDER = "pref_provider";
    public static final String PREF_KEY_MAX_TEGOED = "pref_max_tegoed";
    public static final String PREF_KEY_START_TEGOED = "pref_start_tegoed";
    public static final String PREF_KEY_USERNAME = "pref_username";
    public static final String PREF_KEY_PASSWORD = "pref_password";
    public static final String PREF_KEY_ENABLE_AUTO_UPDATE = "pref_enable_auto_update";
    public static final String PREF_KEY_UPDATE_CHANNEL = "pref_update_channel";
    public static final String PREF_KEY_WIFI_UPDATE_INTERVAL = "pref_wifi_update_interval";
    public static final String PREF_KEY_MOBILE_UPDATE_INTERVAL = "pref_mobile_update_interval";
    public static final String PREF_KEY_LASTEST_UPDATE = "pref_latest_update";
    public static final String PREF_KEY_THEME = "pref_theme";

    public static final String ALARM_MANAGER_SET = "alarm manager set";

    private static final String LOG_TAG = "HaroidApp";
    private static final String SHARED_PREFERENCE_NAME = "nl.haroid_preferences";
    private static final int SHARED_PREFERENCE_MODE = MODE_PRIVATE;

    private static final String VERBRUIK_DAG = "pref_verbruik_dag";

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
        setCustomTheme();
        BalanceRepository balanceRepository = new BalanceRepositoryImpl(new StorageOpenHelperAndroidImpl(this, BalanceRepositoryImpl.DATABASE_NAME, BalanceRepositoryImpl.DATABASE_VERSION));
        this.historyMonitor = new HistoryMonitor(balanceRepository);
        int startBalance = getStartTegoed();
        if (startBalance > 0) {
            Map<Integer, Integer> prefVerbruikMap = new HashMap<Integer, Integer>();
            DecimalFormat decimalFormat = new DecimalFormat("00");
            for (int i=1; i<32; i++) {
                String verbruikKey = VERBRUIK_DAG + decimalFormat.format(i);
                int amountThisDay = sharedPreferences.getInt(verbruikKey, -1);
                if (amountThisDay != -1) {
                    prefVerbruikMap.put(i, amountThisDay);
                    SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                    prefEditor.remove(verbruikKey);
                    prefEditor.commit();
                }
            }
            if (prefVerbruikMap.size() > 0) {
                this.historyMonitor.convertToDatabase(prefVerbruikMap, startBalance);
            }
        }
        INSTANCE = this;
    }

    private void setCustomTheme() {
        setTheme(getCustomTheme());
    }

    private int getCustomTheme() {
        Theme defaultTheme = Theme.DARK;
        int darkTheme = android.R.style.Theme_Black;
        int lightTheme = android.R.style.Theme_Light;
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            defaultTheme = Theme.LIGHT;
            darkTheme = android.R.style.Theme_Holo;
            lightTheme = android.R.style.Theme_Holo_Light;
        }
        Theme chosenTheme = Theme.valueOf(sharedPreferences.getString(PREF_KEY_THEME, defaultTheme.name()));
        if (chosenTheme == Theme.LIGHT) {
            return lightTheme;
        } else {
            return darkTheme;
        }
    }

    public void setCustomTheme(Activity activity) {
        activity.setTheme(getCustomTheme());
    }

    public static HaroidApp getInstance() {
        return INSTANCE;
    }
    
    public static String getProvider() {
        return sharedPreferences.getString(PREF_KEY_PROVIDER, Provider.HOLLANDS_NIEUWE.name());
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
        return Utils.parseInt(sharedPreferences.getString(PREF_KEY_WIFI_UPDATE_INTERVAL, "24"), 24);
    }

    public static int getMobileUpdateInterval() {
        return Utils.parseInt(sharedPreferences.getString(PREF_KEY_MOBILE_UPDATE_INTERVAL, "24"), 24);
    }

    public static int getMaxTegoed() {
        return Utils.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0"), 0);
    }

    public static int getStartTegoed() {
        return Utils.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"), 0);
    }

    public List<HistoryMonitor.UsagePoint> getUsageList() {
        int startBalance = getStartTegoed();
        int maxBalance = getMaxTegoed();
        return this.historyMonitor.getUsageList(startBalance, maxBalance);
    }

    public Stats recalculate() {
        Stats stats = new Stats();
        stats.maxBalance = getMaxTegoed();
        stats.startBalance = getStartTegoed();
        berekenDuurTegoed(stats);
        return stats;
    }

    public void setCurrentBalance(int balance) {
        if (balance >= 0) {
            this.historyMonitor.setTegoed(balance, new Date());
            Date updateDate = new Date();
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            prefEditor.putLong(PREF_KEY_LASTEST_UPDATE, updateDate.getTime());
            prefEditor.commit();
        }
    }

    public int getCurrentBalance() {
        int startBalance = getStartTegoed();
        if (startBalance > 0) {
            int dagInPeriode = Utils.bepaaldDagInPeriode(startBalance);
            return this.historyMonitor.getBalance(dagInPeriode, new Date());
        } else {
            return -1;
        }
    }

    public int getBalanceYesterday() {
        int startBalance = getStartTegoed();
        return this.historyMonitor.getTegoedGisteren(startBalance);
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

    class Stats {
        int startBalance;
        int currentBalance;
        int maxBalance;
        int dayInPeriod;
        int daysToGoInPeriod;
        int maxPeriod;
    }

}
